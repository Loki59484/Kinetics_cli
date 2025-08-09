#javac -d bin -cp "bin:lib/*:images/*" -encoding ISO-8859-1 $(find src -name "*.java")
reverse_barr=("154.9929793" "178.8233301" "156.264509" "165.7457146" "154.9927168" "156.2926018" "20.8191648" "75.31535555" "70.7094409")
file_arr=($1/*)
file_nums=${#file_arr[@]}
if [ -z "$2" ]; then
echo "argument 2 required"
exit
fi
for (( i=0; i<$file_nums; i++ )); do
file="${file_arr[$i]}"
revbar="${reverse_barr[$i]}"
if echo $file | grep -q ".kinp"; then
outname="$(basename "$file")"
#java -cp "bin:lib/*" Kistep --headless calc VTST  -y --moltype bi --tunnelling $2 -sr -sd -r1  /home/loki/Research/Ethyl_propiolate/kisthelp_inputs/ethyl_propiolate_non_planar_2_m062x.kinp -r2 /home/loki/Research/Ethyl_propiolate/kisthelp_inputs/OH-radical_m062.kinp -rpath $file -o "../../Ethyl_propiolate/VTST_$2/${outname/.kinp/.kstp}" -T 290,300,1 -revb $revbar
java -cp "bin:lib/*" Kistep --headless calc VTST  -y --moltype bi --tunnelling $2 -sr -sd -r1  /home/loki/Research/Ethyl_propiolate/kisthelp_inputs/ethyl_propiolate_planar_m062x.kinp -r2 /home/loki/Research/Ethyl_propiolate/kisthelp_inputs/OH-radical_m062.kinp -rpath $file -o "../../Ethyl_propiolate/VTST_$2/${outname/.kinp/.kstp}" -T 290,300,1 -revb $revbar
fi
done
