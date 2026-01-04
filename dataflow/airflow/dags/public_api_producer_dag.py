from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.operators.trigger_dagrun import TriggerDagRunOperator
from airflow.utils.dates import days_ago
from datetime import datetime, timedelta
# import sys
# sys.path.append("/opt/airflow/dataflow")

# from producer import run_kafka_producer   # producer.py 위치 기준

def run_kafka_producer_wrapper(**context):
    from producer import run_kafka_producer
    return run_kafka_producer(**context)

with DAG(
    dag_id="public_api_producer_dag",
    start_date=datetime(2025, 11, 3),
    catchup=False,
    schedule="*/1 * * * *",  # 1분마다 실행
    tags=["producer", "kafka", "api"],
) as dag:

    run_producer = PythonOperator(
        task_id="run_kafka_producer",
        python_callable=run_kafka_producer_wrapper,
    )

    trigger_linkinfo = TriggerDagRunOperator(
        task_id="trigger_linkinfo_dag",
        trigger_dag_id="linkinfo_dag",
        wait_for_completion=False,
        reset_dag_run=True,
    )

    run_producer >> trigger_linkinfo
