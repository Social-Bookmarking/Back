REPOSITORY=/home/ec2-user/app
cd $REPOSITORY

# AWS Parameter Store에서 비밀 정보 읽어오기
export DB_URL=$(aws ssm get-parameter --name "/marksphere/prod/db-url" --with-decryption --query "Parameter.Value" --output text)
export DB_USERNAME=$(aws ssm get-parameter --name "/marksphere/prod/db-username" --with-decryption --query "Parameter.Value" --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name "/marksphere/prod/db-password" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECRET=$(aws ssm get-parameter --name "/marksphere/jwt-secret" --with-decryption --query "Parameter.Value" --output text)
export YOUTUBE_API_KEY=$(aws ssm get-parameter --name "/marksphere/youtube-api-key" --with-decryption --query "Parameter.Value" --output text)
export AWS_ACCESS_KEY=$(aws ssm get-parameter --name "/marksphere/aws-access-key" --with-decryption --query "Parameter.Value" --output text)
export AWS_SECRET_KEY=$(aws ssm get-parameter --name "/marksphere/aws-secret-key" --with-decryption --query "Parameter.Value" --output text)
export REDIS_HOST=$(aws ssm get-parameter --name "/marksphere/redis-host" --with-decryption --query "Parameter.Value" --output text)
export AWS_FUNCTION_NAME=$(aws ssm get-parameter --name "/marksphere/aws-function-name" --with-decryption --query "Parameter.Value" --output text)

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep '.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

echo "> $JAR_PATH 에 실행 권한 추가"
chmod +x $JAR_PATH

echo "> 현재 구동 중인 애플리케이션 PID 확인"
CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)

if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> 구동 중인 애플리케이션 종료 (PID: $CURRENT_PID)"
  sudo kill -15 $CURRENT_PID
  sleep 5
fi

echo "> $JAR_PATH 배포"
nohup java -jar \
    -Dspring.profiles.active=prod \
    $JAR_PATH > /home/ec2-user/nohup.out 2>&1 &