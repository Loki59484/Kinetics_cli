#javac -d bin -cp "bin:lib/*:images/*" -encoding ISO-8859-1 $(find src -name "*.java")
for i in {0..9};do
echo "$(find ../../Ethyl_propiolate/Ethyl_prop_non-planar/Transition_states/ -name "ts$i*ircpoint*.kinp" |sort |tr '\n' ',' | sed 's/,$//')"
echo "\n \n"
#java -cp "bin:lib/*" Kistep --headless calc Rpath -y --pts 21 --kinpfiles="$(find ../../Ethyl_propiolate/Ethyl_prop_non-planar/Transition_states/ -name "ts$i*ircpoint*.kinp" |sort |tr '\n' ',' | sed 's/,$//')" --ircpoints="$(seq -1.0 0.1 1.0 | tr '\n' ',' | sed 's/,$//')" -o "~/Research/Ethyl_propiolate/kisthelp_inputs/Path$1";
 done