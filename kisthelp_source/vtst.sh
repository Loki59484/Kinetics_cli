javac -d bin -cp "bin:lib/*:images/*" -encoding ISO-8859-1 $(find src -name "*.java")
for files in "$1"/* ;
java -cp "bin:lib/*" Kistep --headless calc vtst-Wig --moltype bi -r1 ../../Ethyl_propiolate/Ethyl_prop_non-planar/Reactants/ethyl_propiolate_non_planar_2_m062x.log -r2 "../../Ethyl_propiolate/Ethyl_prop_non-planar/Reactants/OH-radical_m062.out" -rpath $files -o "../../Ethyl_propiolate/VTST_Wig/$(basename "$files" .kinp)" -T 200,500,25
done