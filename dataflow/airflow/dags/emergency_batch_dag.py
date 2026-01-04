from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta
# import sys
# sys.path.append("/opt/airflow/dataflow")
# from preprocessing.emergency_batch import run_emergency_batch

default_args = {
    "owner": "navisafe",
    "depends_on_past": False,
    "retries": 1,
    "retry_delay": timedelta(minutes=5),
}

def run_emergency_batch_wrapper(**context):
    from preprocessing.emergency_batch import run_emergency_batch
    return run_emergency_batch(**context)
    
with DAG(
    dag_id="emergency_alert_batch_dag",
    default_args=default_args,
    description="Redis → MySQL Emergency Alert Batch",
    start_date=datetime(2025, 1, 1),
    schedule="*/5 * * * *",  # 5분 주기
    catchup=False,
    tags=["emergency", "redis", "mysql"],
) as dag:

    emergency_batch_task = PythonOperator(
        task_id="emergency_alert_mysql_batch",
        python_callable=run_emergency_batch_wrapper,
        op_kwargs={"batch_size": 200},
    )

    emergency_batch_task
