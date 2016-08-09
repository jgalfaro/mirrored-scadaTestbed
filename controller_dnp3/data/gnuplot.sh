#set terminal jpeg size 1680, 918 enhanced font "Helvetica,20"      
set terminal jpeg size 3000, 918 enhanced font "Helvetica,20"
set grid
#set log y
set key left top
set title "G Total"
set xlabel "Time"                      
set ylabel "G Total"                    
plot 'gtotal' using 1:2 with linespo pt 3 lc rgb "red" lw 2 title "G Total"
