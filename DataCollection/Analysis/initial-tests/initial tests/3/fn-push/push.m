for c = 1:23
  path = ['Float',int2str(c),'.txt']
  ydata = load (path); 
    x = linspace(0,4095,4096);
    figure(c)
    plot(x,ydata(:,1));
end