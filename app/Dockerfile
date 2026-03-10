FROM python:3.9.6-slim

# 设置工作目录为 /code，应用代码放在 /code/app/
# 这样 `from app.config import ...` 的导入路径可以正常工作
WORKDIR /code

# 先单独复制 requirements.txt，利用 Docker 层缓存
# 只有依赖变化时才重新安装，代码改动不会触发 pip install
# 注意：build context 为项目根目录（docker-compose.yml 中 context: .）
COPY app/requirements.txt /code/

RUN pip install --no-cache-dir -r requirements.txt

# 复制应用代码和 Realm 初始化脚本
COPY app/ /code/app/
COPY scripts/setup_realm.py /code/setup_realm.py

# entrypoint：启动前自动初始化 Keycloak Realm
COPY app/entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r//' /entrypoint.sh && chmod +x /entrypoint.sh

# 非 root 用户运行（安全最佳实践）
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

EXPOSE 8000

ENTRYPOINT ["/entrypoint.sh"]
