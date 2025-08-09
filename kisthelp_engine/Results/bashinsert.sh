#!/bin/bash

# This script reads data from standard input, where each line is expected
# to be in the format "filename:content_to_insert".
# It then inserts the content into the specified file after a specific line.
#
# --- HOW TO USE ---
# 1. Save this script as 'batch_insert.sh'
# 2. Make it executable: chmod +x batch_insert.sh
# 3. Pipe your grep command directly into it, like this:
#
# ls | xargs -I {} grep -H 298.0 {} | ./batch_insert.sh
#

echo "--- Starting batch insertion process ---"

# The `while read` loop processes each line coming from the pipe.
# IFS= prevents leading/trailing whitespace from being trimmed.
# -r prevents backslash escapes from being interpreted.
while IFS= read -r line; do
    # Use Bash's parameter expansion to split the line at the first colon.
    # This is robust and efficient.
    filename="/home/loki/Research/Ethyl_propiolate/Results_Wig/${line%%:*}"
    content_to_insert="${line#*:}"

    # Check if the extracted filename points to an actual file to prevent errors.
    if [ -f "$filename" ]; then
        echo "-> Inserting line into: $filename"
        
        # Use sed to find the line STARTING WITH (^) "275.000"
        # and append (a) the new content on the next line.
        # The -i flag modifies the file in-place.
        # We escape the dot in "275\.000" to treat it as a literal period.
        sed -i "/^275\.000/a $content_to_insert" "$filename"
    else
        echo "-> SKIPPING: File not found at '$filename'"
    fi
done
