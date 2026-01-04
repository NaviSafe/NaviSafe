from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.utils.dates import days_ago
import sys
# sys.path.append("/opt/airflow/dataflow")

# from preprocessing.outbreak_batch import save_from_redis_to_mysql

def save_from_redis_to_mysql_wrapper():
    from preprocessing.outbreak_batch import save_from_redis_to_mysql
    return save_from_redis_to_mysql()

with DAG(
    dag_id="outbreak_batch_dag",
    # start_date=days_ago(1),
    schedule=None,
    catchup=False,
) as dag:

    batch_save = PythonOperator(
        task_id="save_batch_to_mysql",
        python_callable=save_from_redis_to_mysql_wrapper,
    )
