dir_name=dynviz-0.0.3-release

cd ..

if [ $# -lt 2 ]; then
  echo Usage: build output_directory profile [profile2 ...]
  echo where profile can be \'win32\', \'win64\', \'linux32\', \'linux64\' or \'macosx\'
  exit 1
fi

out=$1
shift

for profile in "$@"; do
  echo Building profile: $profile
  mvn clean install -P$profile
  rm -rf "$out/$dir_name-$profile"
  cp -r "build/target/$dir_name" "$out/$dir_name-$profile"
done