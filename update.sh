sh stop.sh

git config user.email "1004w455@naver.com"
git config user.name "1004w455"

echo 'git pull'
git fetch --all
git reset --hard origin/master
git pull origin master

echo 'mvn package'
mvn -Dmaven.test.skip=true package

sh start.sh

echo 'END update.sh'
