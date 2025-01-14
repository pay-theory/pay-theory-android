
for file in *; do
  echo "Processing $file"
  gpg -u "Pay Theory <support@paytheory.com>" -ab $file
  md5sum "$file" | cut -d " " -f 1 > "$file".md5
  sha1sum "$file" | cut -d " " -f 1 > "$file".sha1
done
