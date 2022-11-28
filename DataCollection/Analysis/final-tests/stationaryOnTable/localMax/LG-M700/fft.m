ydata = load ("50.txt"); 
x0 = linspace(0,2047,2048);
x = x0 * 48000/4096/1000;
plot(x,ydata);
xlabel('Frequency (kHz)');
ylabel('Amplitude');
title('Stationary');
%ylim([-0.000001 1]);