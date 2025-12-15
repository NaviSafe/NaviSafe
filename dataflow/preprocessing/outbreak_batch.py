import json
import time
import logging
import mysql.connector
from utils.redis_utils import RedisClient

# -----------------------------------
# 로깅 / Redis 설정
# -----------------------------------
log = logging.getLogger("airflow.task")

redis_client = RedisClient(host="redis", port=6379, db=0)
DB_BATCH_SIZE = 100  # Redis → MySQL 배치 크기


def save_from_redis_to_mysql():
    # -----------------------------------
    # 1. Redis에서 데이터 수집
    # -----------------------------------
    batch = []
    for _ in range(DB_BATCH_SIZE):
        item = redis_client.lpop("db_queue")
        if item is None:
            break
        batch.append(json.loads(item))

    if not batch:
        log.info("[INFO] No data in Redis, sleeping 5s...")
        time.sleep(5)
        return

    # -----------------------------------
    # 2. acc_id 기준 중복 제거
    # -----------------------------------
    unique_batch = list({
        d["acc_id"]: d
        for d in batch
        if d.get("acc_id")
    }.values())

    # -----------------------------------
    # 3. 필수 컬럼 기준 필터링
    # (하나라도 NULL이면 전체 테이블 삽입 제외)
    # -----------------------------------
    filtered_batch = [
        d for d in unique_batch
        if d.get("acc_id")
        and d.get("acc_type")
        and d.get("acc_dtype")
    ]

    skipped = len(unique_batch) - len(filtered_batch)
    if skipped > 0:
        log.warning(f"[SKIP] 필수 컬럼 누락으로 제외된 데이터: {skipped}건")

    if not filtered_batch:
        log.info("[INFO] 유효한 데이터가 없어 DB 작업을 종료합니다.")
        return

    # -----------------------------------
    # 4. MySQL 연결
    # -----------------------------------
    conn = mysql.connector.connect(
        host="mysql",
        user="user",
        password="userpass",
        database="toy_project"
    )
    cursor = conn.cursor()

    try:
        # -----------------------------------
        # OUTBREAK_OCCURRENCE
        # -----------------------------------
        cursor.executemany("""
            INSERT IGNORE INTO OUTBREAK_OCCURRENCE
                (ACC_ID, occr_date_time, exp_clr_date_time)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE
                occr_date_time = VALUES(occr_date_time),
                exp_clr_date_time = VALUES(exp_clr_date_time)
        """, [
            (d["acc_id"], d.get("occr_date_time"), d.get("exp_clr_date_time"))
            for d in filtered_batch
        ])
        log.info(f"[DB] OUTBREAK_OCCURRENCE 삽입 ({len(filtered_batch)}건)")

        # -----------------------------------
        # OUTBREAK_DETAIL_CODE
        # -----------------------------------
        cursor.execute("SELECT ACC_DTYPE FROM OUTBREAK_DETAIL_CODE_NAME")
        valid_dtypes = {r[0] for r in cursor.fetchall()}

        batch_detail = [
            d for d in filtered_batch
            if d["acc_dtype"] in valid_dtypes
        ]

        if batch_detail:
            cursor.executemany("""
                INSERT IGNORE INTO OUTBREAK_DETAIL_CODE
                    (OUTBREAK_ACC_ID, ACC_DTYPE)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    ACC_DTYPE = VALUES(ACC_DTYPE)
            """, [
                (d["acc_id"], d["acc_dtype"])
                for d in batch_detail
            ])
            log.info(f"[DB] OUTBREAK_DETAIL_CODE 삽입 ({len(batch_detail)}건)")

        # -----------------------------------
        # OUTBREAK_CODE
        # -----------------------------------
        cursor.executemany("""
            INSERT IGNORE INTO OUTBREAK_CODE
                (OUTBREAK_ACC_ID, ACC_TYPE)
            VALUES (%s, %s)
            ON DUPLICATE KEY UPDATE
                ACC_TYPE = VALUES(ACC_TYPE)
        """, [
            (d["acc_id"], d["acc_type"])
            for d in filtered_batch
        ])
        log.info(f"[DB] OUTBREAK_CODE 삽입 ({len(filtered_batch)}건)")

        # -----------------------------------
        # OUTBREAK_LINK
        # -----------------------------------
        cursor.execute("SELECT LINK_ID FROM LINK_ID")
        valid_links = {r[0] for r in cursor.fetchall()}

        batch_link = [
            d for d in filtered_batch
            if d.get("link_id") in valid_links
        ]

        if batch_link:
            cursor.executemany("""
                INSERT IGNORE INTO OUTBREAK_LINK
                    (OUTBREAK_ACC_ID, LINK_ID)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    LINK_ID = VALUES(LINK_ID)
            """, [
                (d["acc_id"], d["link_id"])
                for d in batch_link
            ])
            log.info(f"[DB] OUTBREAK_LINK 삽입 ({len(batch_link)}건)")

        # -----------------------------------
        # MAP_GPS
        # -----------------------------------
        cursor.executemany("""
            INSERT IGNORE INTO MAP_GPS
                (OUTBREAK_ACC_ID, GRS80TM_X, GRS80TM_Y)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE
                GRS80TM_X = VALUES(GRS80TM_X),
                GRS80TM_Y = VALUES(GRS80TM_Y)
        """, [
            (d["acc_id"], d["grs80tm_x"], d["grs80tm_y"])
            for d in filtered_batch
            if d.get("grs80tm_x") and d.get("grs80tm_y")
        ])

        # -----------------------------------
        # ACC_ALERTS
        # -----------------------------------
        cursor.executemany("""
            INSERT IGNORE INTO ACC_ALERTS
                (OUTBREAK_ACC_ID, ACC_INFO)
            VALUES (%s, %s)
            ON DUPLICATE KEY UPDATE
                ACC_INFO = VALUES(ACC_INFO)
        """, [
            (d["acc_id"], d["acc_info"])
            for d in filtered_batch
            if d.get("acc_info")
        ])

        conn.commit()
        log.info("[SYSTEM] Redis → MySQL 배치 저장 완료")

    except Exception as e:
        conn.rollback()
        log.error(f"[ERROR] DB 배치 처리 실패: {e}")
        raise

    finally:
        cursor.close()
        conn.close()

    log.info("[SYSTEM] MySQL 연결 종료")