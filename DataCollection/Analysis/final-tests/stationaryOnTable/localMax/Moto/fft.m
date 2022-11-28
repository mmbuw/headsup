ydata = load ("110.txt"); 
%x0 = linspace(0,2048,2049);
%x = x0 * 48000/4096;
x=0:2048;
xlim([0 2048]);
plot(x,ydata(:,1));