for c = 50:51
  path = [int2str(c),'.txt']
  ydata = load (path); 
  xlabel('hii') 
ylabel('Sine and Cosine Values')
    x = linspace(1,2049,2049);
    figure(c)
    plot(x,ydata(:,1));
end