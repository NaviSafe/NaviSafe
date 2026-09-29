from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.python import PythonOperator


def run_kafka_producer_wrapper():
    """
    공공 API 데이터를 수집한 뒤 Kafka로 전송한다.

    DAG 파일이 파싱될 때 외부 모듈 import 오류가 발생하지 않도록
    실제 실행 시점에 모듈을 import한다.
    """
    from producer.kafka_producer import run_kafka_producer

    return run_kafka_producer()


default_args = {
    "owner": "airflow",
    "retries": 2,
    "retry_delay": timedelta(seconds=30),
}


with DAG(
    dag_id="public_api_producer_dag_v2",
    description="공공 API 데이터를 수집하여 Kafka로 전송",
    default_args=default_args,
    start_date=datetime(2025, 11, 3),
    schedule="*/1 * * * *",
    catchup=False,
    max_active_runs=1,
    tags=["producer", "kafka", "api"],
) as dag:

    run_producer = PythonOperator(
        task_id="run_kafka_producer",
        python_callable=run_kafka_producer_wrapper,
        execution_timeout=timedelta(minutes=1),
    )