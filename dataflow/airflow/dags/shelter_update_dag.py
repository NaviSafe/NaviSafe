from airflow import DAG
from airflow.operators.bash import BashOperator
from datetime import datetime, timedelta
import os 
import sys, os
#sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'plugins'))
#sys.path.append("/opt/airflow/plugins")
default_args = {
    'owner': 'data_engineer',
    'depends_on_past': False,
    'email_on_failure': True,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=10),
}

with DAG(
    dag_id='update_shelter_data_monthly',
    default_args=default_args,
    description='서울시 대피소/쉼터/침수예상도 데이터를 매월 갱신',
    schedule_interval='@monthly',  # 한 달에 한 번 실행
    start_date=datetime(2025, 1, 1),
    catchup=False,
    tags=['shelter', 'seoul', 'api', 'mysql'],
) as dag:

    update_shelter_data = BashOperator(
        task_id='run_shelter_worker',
        bash_command='python /opt/airflow/plugins/airflow_utils/shelter_worker.py',
        env={
            'EARTHQUAKE_SHELTER_API_KEY': os.getenv('EARTHQUAKE_SHELTER_API_KEY'),
            'EARTHQUAKE_OUTDOOR_API_KEY': os.getenv('EARTHQUAKE_OUTDOOR_API_KEY'),
            'SUMMER_SHELTER_API_KEY': os.getenv('SUMMER_SHELTER_API_KEY'),
            'FINE_DUST_SHELTER_API_KEY': os.getenv('FINE_DUST_SHELTER_API_KEY'),
        }
    )

    update_shelter_data
