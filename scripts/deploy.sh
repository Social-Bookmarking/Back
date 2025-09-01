REPOSITORY=/home/ec2-user/app
cd $REPOSITORY

APP_NAME=MarkSphere
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep '.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

echo "> $JAR_PATH 에 실행 권한 추가"
chmod +x $JAR_PATH

PORT=8080
CURRENT_PID=$(lsof -t -i:$PORT)


if [ -z "$CURRENT_PID" ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> 정상 종료 시도: kill -15 $CURRENT_PID"
  sudo kill -15 $CURRENT_PID
  sleep 3

  if kill -0 $CURRENT_PID > /dev/null 2>&1
  then
    echo "> 프로세스가 종료되지 않아 강제 종료합니다: kill -9 $CURRENT_PID"
    sudo kill -9 $CURRENT_PID
  else
    echo "> 정상적으로 종료되었습니다."
  fi
fi

echo "> $JAR_PATH 배포"
nohup java -jar \
    -Dspring.profiles.active=dev \
    $JAR_PATH > /home/ec2-user/nohup.out 2>&1 &