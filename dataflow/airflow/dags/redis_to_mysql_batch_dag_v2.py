from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.python import PythonOperator


def run_linkinfo_worker_wrapper():
    """
    Redis의 link_queue 데이터를 읽어 LINK_ID 관련 데이터를 처리한다.
    """
    from preprocessing.linkinfo_worker import run_linkinfo_worker

    return run_linkinfo_worker()


def save_from_redis_to_mysql_wrapper():
    """
    Redis의 db_queue 데이터를 읽어 MySQL에 배치 적재한다.
    """
    from preprocessing.outbreak_batch import save_from_redis_to_mysql

    return save_from_redis_to_mysql()


default_args = {
    "owner": "airflow",
    "retries": 2,
    "retry_delay": timedelta(minutes=1),
}


with DAG(
    dag_id="redis_to_mysql_batch_dag",
    description="Redis 데이터를 처리하여 MySQL에 배치 적재",
    default_args=default_args,
    start_date=datetime(2025, 11, 3),
    schedule="*/5 * * * *",
    catchup=False,
    max_active_runs=1,
    tags=["redis", "mysql", "batch"],
) as dag:

    process_linkinfo = PythonOperator(
        task_id="process_linkinfo",
        python_callable=run_linkinfo_worker_wrapper,
        execution_timeout=timedelta(minutes=3),
    )

    save_batch_to_mysql = PythonOperator(
        task_id="save_batch_to_mysql",
        python_callable=save_from_redis_to_mysql_wrapper,
        execution_timeout=timedelta(minutes=3),
    )

    process_linkinfo >> save_batch_to_mysql