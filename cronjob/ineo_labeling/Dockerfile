FROM python:3.11-alpine

WORKDIR /app
ADD .ineo.env /app/.ineo.env
ADD ineo_labeling.py /app/ineo_labeling.py
ADD ineo_labeling_utils.py /app/ineo_labeling_utils.py
ADD ineo_classes.py /app/ineo_classes.py
ADD requirements.txt /app/requirements.txt

RUN pip install --no-cache-dir -r requirements.txt
#ENTRYPOINT ["/bin/sh", "-c", "tail -f /dev/null"]
ENTRYPOINT ["python", "/app/ineo_labeling.py"]
