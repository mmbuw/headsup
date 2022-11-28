for c = 15:16
  path = [int2str(c),'.txt']
  ydata = load (path); 
  xlabel('hii') 
ylabel('Sine and Cosine Values')
    x = linspace(0,2048,2049);
    figure(c)
    plot(x,ydata(:,1));
end