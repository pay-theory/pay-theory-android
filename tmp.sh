echo "initial pt_release_version ${pt_release_version}"
COMMIT=$(git rev-parse HEAD)
echo "commit ${COMMIT}"
branch=$(git branch -a --contains $COMMIT  | head -n 1) | cut -d " " -f 2
echo "branch ${branch}"
release=$(echo $branch | cut -d "-" -f 2)
echo "release ${release}"
if [ "$pt_release_version" == "3.0" ]; then
    pt_release_version=$(echo $branch | cut -d "-" -f 2)
    echo "pt_release_version set to ${pt_release_version}"
else
    echo "no need to change ${pt_release_version}"
fi

echo "Building Release ${pt_release_version}"