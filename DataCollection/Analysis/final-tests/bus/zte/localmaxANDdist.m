x = 1:103;
 
%x = x*4096/48000;
y = importdata ('DistToPilotIndex.txt');
 
stem (x , y(:,1))
xlabel('File number');
ylabel('Distance to pilot tone index (bins)');
title('ZTE Axon 7');
ylim([0 99]);
xlim([0 105]);
set(gca,'YTick', 0:2:99);
yyaxis left
 
hold on
%x = x*4096/48000;
y2 = importdata ('LocalMaxRatio.txt');
plot (x, y2(:,1)*1000, 'r')
yyaxis right
ylim([0 99]);
%xlim([1 93]);
 
set(gca,'YTick', 0:2:99);
grid on
ylabel('(Local Max ampl./ Main Peak ampl.) *10^3');
hold on
 
%y3 = ones(69);
%plot (x, y3(:,1) - 0.15, ':k');
 
%y4 = y3 + 9;
%plot (x, y4(:,1), ':k');
 
%y5 = y3 + 12;
%plot (x, y5(:,1), ':k');
%hold off
%legend('Index distance','Local ratio');