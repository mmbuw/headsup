x = linspace(1,158,158);
y1 = load ("LocalMaxRatio.txt"); ;
y2 = load ("DistToPilotIndex.txt"); 
%ax = plotyy (x, y1, x , y2, @plot, @semilogy);
ax = plotyy (x, y1(:,1), x , y2(:,1) );
xlabel ("Sample No");
ylabel (ax(1), "LocalMaxRatio");
ylabel (ax(2), "DistToPilotIndex");
%plot (x, y1, '-r');