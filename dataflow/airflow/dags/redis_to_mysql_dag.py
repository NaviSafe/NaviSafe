# redis_to_mysql_dag.py
from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta
import sys

sys.path.append("/opt/airflow/dataflow")  # producer.py가 있는 경로 추가
sys.path.append("/opt/airflow/dataflow/preprocessing")  # outbreak_preprocessing.py 경로 추가

def load_producer():
    from producer import run_kafka_producer
    return run_kafka_producer

def load_outbreak_preprocessing():
    from outbreak_preprocessing import save_from_redis_to_mysql
    return save_from_redis_to_mysql


default_args = {
    'owner': 'navisafe',
    'depends_on_past': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=1),
}

with DAG(
    dag_id='redis_to_mysql_pipeline',
    default_args=default_args,
    description='공공데이터 → Kafka → Redis → MySQL 자동 파이프라인',
    schedule_interval='*/1 * * * *',
    start_date=datetime(2025, 11, 3),
    catchup=False,
    tags=['redis', 'mysql', 'kafka'],
) as dag:

    produce_task = PythonOperator(
        task_id='run_kafka_producer',
        python_callable=lambda: load_producer()(),
    )

    outbreak_save_task = PythonOperator(
        task_id='outbreak_save_redis_to_mysql',
        python_callable=lambda: load_outbreak_preprocessing()(),
    )

    produce_task >> outbreak_save_task
