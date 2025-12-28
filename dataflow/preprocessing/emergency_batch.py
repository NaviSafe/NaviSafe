import json
import logging
from airflow.providers.mysql.hooks.mysql import MySqlHook
from utils.redis_utils import RedisClient

log = logging.getLogger("airflow.task")

REDIS_QUEUE_KEY = "emergency_alert_queue"
MYSQL_CONN_ID = "navisafe_mysql"   # Airflow Connection ID
MYSQL_TABLE = "EMERGENCY_ALERT"

def run_emergency_batch(batch_size=100):
    """
    Redis → MySQL 적재 전용 배치 컴포넌트
    Airflow PythonOperator에서 호출
    """

    log.info("[SYSTEM] Emergency Batch 시작")

    redis_client = RedisClient(host="redis", port=6379, db=0)
    mysql_hook = MySqlHook(mysql_conn_id=MYSQL_CONN_ID)

    conn = mysql_hook.get_conn()
    cursor = conn.cursor()

    insert_sql = f"""
        INSERT INTO {MYSQL_TABLE} (
            SN,
            CRT_DT,
            MSG_CN,
            RCPTN_RGN_NM,
            EMRG_STEP_NM,
            DST_SE_NM,
            REG_YMD,
            MDFCN_YMD
        )
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
        ON DUPLICATE KEY UPDATE
            MSG_CN = VALUES(MSG_CN),
            MDFCN_YMD = VALUES(MDFCN_YMD)
    """

    inserted = 0

    try:
        for _ in range(batch_size):
            item = redis_client.lpop_list(REDIS_QUEUE_KEY)
            if not item:
                break

            if isinstance(item, str):
                item = json.loads(item)

            values = (
                item.get("SN"),
                item.get("CRT_DT"),
                item.get("MSG_CN"),
                item.get("RCPTN_RGN_NM"),
                item.get("EMRG_STEP_NM"),
                item.get("DST_SE_NM"),
                item.get("REG_YMD"),
                item.get("MDFCN_YMD"),
            )

            cursor.execute(insert_sql, values)
            inserted += 1

        conn.commit()
        log.info(f"[SUCCESS] Emergency Alert {inserted}건 MySQL 적재 완료")

    except Exception as e:
        conn.rollback()
        log.error(f"[ERROR] Emergency Batch 실패: {e}")
        raise

    finally:
        cursor.close()
        conn.close()
