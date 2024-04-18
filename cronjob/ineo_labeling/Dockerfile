FROM python:3.11-alpine

WORKDIR /app
COPY .ineo.env /app/.ineo.env
COPY ineo_labeling.py /app/ineo_labeling.py
COPY ineo_labeling_utils.py /app/ineo_labeling_utils.py
COPY ineo_classes.py /app/ineo_classes.py
COPY requirements.txt /app/requirements.txt

RUN pip install --no-cache-dir -r requirements.txt
#ENTRYPOINT ["/bin/sh", "-c", "tail -f /dev/null"]
ENTRYPOINT ["python", "/app/ineo_labeling.py"]
