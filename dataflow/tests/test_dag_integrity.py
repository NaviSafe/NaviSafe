import glob
import importlib.util
import os
import pytest
from airflow.models import DAG

BASE_DIR = os.path.dirname(os.path.dirname(__file__))

DAG_PATH = os.path.join(
    BASE_DIR,
    "airflow",
    "dags",
    "*.py"
)

dag_files = glob.glob(DAG_PATH)

@pytest.mark.parametrize("dag_file", dag_files)
def test_dag_integrity(dag_file):
    """
    1. DAG 파일 import 가능 여부
    2. DAG 객체 최소 1개 이상 생성 여부
    """

    module_name = os.path.basename(dag_file).replace(".py", "")
    spec = importlib.util.spec_from_file_location(module_name, dag_file)
    module = importlib.util.module_from_spec(spec)

    # import 에러 발생 시 여기서 바로 실패
    spec.loader.exec_module(module)

    dag_objects = [
        obj for obj in vars(module).values()
        if isinstance(obj, DAG)
    ]

    assert len(dag_objects) > 0, f"{dag_file} has no DAG object"
