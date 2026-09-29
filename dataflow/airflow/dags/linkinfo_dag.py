from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.operators.trigger_dagrun import TriggerDagRunOperator
from airflow.utils.dates import days_ago
import sys
# sys.path.append("/opt/airflow/dataflow")

# from preprocessing.linkinfo_worker import run_linkinfo_worker

def run_linkinfo_worker_wrapper():
    from preprocessing.linkinfo_worker import run_linkinfo_worker
    return run_linkinfo_worker()

with DAG(
    dag_id="linkinfo_dag",
    # start_date=days_ago(1),
    schedule=None,
    catchup=False,
) as dag:

    process_linkinfo = PythonOperator(
        task_id="process_linkinfo",
        python_callable=run_linkinfo_worker_wrapper,
    )

    trigger_batch = TriggerDagRunOperator(
        task_id="trigger_outbreak_batch_dag",
        trigger_dag_id="outbreak_batch_dag",
        wait_for_completion=False,
    )

    process_linkinfo >> trigger_batch
