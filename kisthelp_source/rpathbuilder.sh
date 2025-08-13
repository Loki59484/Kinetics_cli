#javac -d bin -cp "bin:lib/*:images/*" -encoding ISO-8859-1 $(find src -name "*.java")
kinp_files=$(ls $1 | grep ".kinp" |xargs -I {} echo "$1/{}" | tr "\n" ",")
coords=$(seq -1 0.1 1 | tr "\n" ",")
output="PATH_$(basename $1 | grep -oE '[0-9]+')"
java -cp "bin:lib/*" Kistep --headless calc Rpath -y --kinpfiles=$kinp_files --pts 21 --ircpoints $coords -o "/home/aayush/Research/Ethyl_propiolate/kisthelp_inputs/$output"