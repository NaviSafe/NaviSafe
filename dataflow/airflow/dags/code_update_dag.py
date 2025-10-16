from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta
from pendulum import timezone
import sys, os
#sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'plugins'))
sys.path.append("/opt/airflow/plugins")
# plugins 경로에 있는 모듈 import
from airflow_utils.outbreak_code_name import update_outbreak_code_name
from airflow_utils.outbreak_detail_code_name import update_outbreak_detail_code_name
from airflow_utils.region_name import update_region_code_name


default_args = {
    'owner': 'navisafe',
    'depends_on_past': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=10),
}

with DAG(
    dag_id='daily_code_update',
    default_args=default_args,
    description='매일 코드명 테이블 갱신',
    schedule_interval='@daily',  # 매일 00시
    start_date=datetime(2025, 10, 14, tzinfo=timezone("Asia/Seoul")),
    catchup=True,
    tags=['update', 'daily', 'code']
) as dag:

    task_outbreak_code = PythonOperator(
        task_id='update_outbreak_code_name',
        python_callable=update_outbreak_code_name
    )

    task_outbreak_detail = PythonOperator(
        task_id='update_outbreak_detail_code_name',
        python_callable=update_outbreak_detail_code_name
    )

    task_region = PythonOperator(
        task_id='update_region_name',
        python_callable=update_region_code_name
    )

    # 실행 순서: 병렬 실행
    [task_outbreak_code, task_outbreak_detail, task_region]
