% Created by Eugene M. Izhikevich, February 25, 2003
% Excitatory neurons    Inhibitory neurons
Ne=800;
Ni=200;
re=rand(Ne,1); % 800 * 1
ri=rand(Ni,1); % 200 * 1
a=[0.02*ones(Ne,1);     0.02+0.08*ri]; % row-wise combine, 1000 * 1
b=[0.2*ones(Ne,1);      0.25-0.05*ri]; % row-wise combine, 1000 * 1
c=[-65+15*re.^2;        -65*ones(Ni,1)]; % row-wise combine, 1000 * 1
d=[8-6*re.^2;           2*ones(Ni,1)]; % row-wise combine, 1000 * 1
S=[0.5*rand(Ne+Ni,Ne) .* (rand(Ne+Ni,Ne) < 1),  -1*rand(Ne+Ni,Ni) .* (rand(Ne+Ni, Ni) < 1)]; % column-wise combine, 1000 * 1000

%figure
%imagesc(S)
 
v=-65*ones(Ne+Ni,1);    % Initial values of v, 1000 * 1
u=b.*v;                 % Initial values of u, 1000 * 1
firings=[];             % spike timings

for t=1:1000            % simulation of 1000 ms
  I=[5*randn(Ne,1);2*randn(Ni,1)]; % thalamic input, 1000 * 1
  fired=find(v>=30);    % indices of spikes
  firings=[firings; t+0*fired,fired]; % two columns, time and id of firing neuron
  v(fired)=c(fired);
  u(fired)=u(fired)+d(fired);
  I=I+sum(S(:,fired),2);
  v=v+0.5*(0.04*v.^2+5*v+140-u+I); % step 0.5 ms
  v=v+0.5*(0.04*v.^2+5*v+140-u+I); % for numerical
  u=u+a.*(b.*v-u);                 % stability
end;
figure
plot(firings(:,1),firings(:,2),'.');
