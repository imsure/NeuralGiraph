% Created by Eugene M. Izhikevich, February 25, 2003
% Modified by Nicholas D. Spielman, Shuo Yang, Jadin C. Jackson
% Excitatory neurons Inhibitory neurons
%
ce=800; ci=200; gpi=100;
re=rand(ce,1); ri=rand(ci+gpi,1);
a=[0.02*ones(ce,1); 0.25*rand(ci,1); 1.75*rand(gpi,1)];
b=[0.2*ones(ce,1); 0.25-0.05*ri];
c=[-65+15*re.^2; -65*ones(ci+gpi,1)];
d=[8-6*re.^2; 2*ones(ci+gpi,1)];

S=[0.65*rand(ce+ci,ce),-0.5*rand(ce+ci,ci),-0.5*rand(ce+ci,gpi);... % corticocortical/pallidal connections *includes hyperdirect pathway w/o STN
   0.65*rand(gpi,ce),zeros(gpi,ci),-0.5*rand(gpi,gpi) ];        %

figure;
imagesc(S);
xlabel('Neuronal Input')
ylabel('Current Neuron')
title('Synaptic weight matrix')

v=-65*ones(ce+ci+gpi,1); % Initial values of v
v_records = [zeros(ce+ci+gpi, 1000)];

u=b.*v; % Initial values of u
firings=[]; % spike timings
mFR = zeros(1,1000); %mean firing rate
ce_mFR = zeros(1,1000);
ci_mFR = zeros(1,1000);
gpi_mFR = zeros(1,1000);

for t=1:1000 % simulation of 1000 ms
I=[5.75*randn(ce,1);5*randn(ci,1);6*randn(gpi,1)]; % thalamic input/open conductance
fired=find(v>=30);% indices of spikes/which neuron fired

ce_fired=find(v(1:ce,:)>=30); % indices of spikes in cortical excitatory population
ci_fired=find(v(ce+1:ce+ci,:)>=30); % spikes in cortical interneuron population
gpi_fired=find(v(ce+ci+1:ce+ci+gpi,:)>=30); % spikes in globus pallidus internus population

firings=[firings; t+0*fired,fired];

v(fired)=c(fired);
v_records(:,t) = v;
u(fired)=u(fired)+d(fired);
I=I+sum(S(:,fired),2);
v=v+0.5*(0.04*v.^2+5*v+140-u+I); % step 0.5 ms
v=v+0.5*(0.04*v.^2+5*v+140-u+I); % for numerical
u=u+a.*(b.*v-u); % stability

mFR(t)=length(fired)/(ce+ci+gpi);  %mFR = the mean number of spikes for that msec or time-step 
                                   %(i.e. number of total spikes in population divided by number of cells)
ce_FR(t)=length(ce_fired);
ci_FR(t)=length(ci_fired);
gpi_FR(t)=length(gpi_fired);
end;
%disp(v_records(1,:));


figure; %includes 9 subplots described below

%v vs time plots for each neuron type
subplot(3,3,1)
plot(v_records(1,:));
xlabel('Time')
ylabel('Membrane Voltage')
title('Cortical Excitatory Firing Rate')

subplot(3,3,2)
plot(v_records(ce+1,:));
xlabel('Time')
ylabel('Membrane Voltage')
title('Cortical Interneuron Firing Rate')

subplot(3,3,3)
plot(v_records(ce+ci+1,:));
xlabel('Time')
ylabel('Membrane Voltage')
title('Globus Pallidus Internus Firing Rate')

%mean v vs time for each neuron type
subplot(3,3,4)
plot(mean(v_records(1:ce,:)));
xlabel('Time')
ylabel('Mean Membrane Potential')
title('Mean Cortical Excitatory Membrane Potential')

subplot(3,3,5)
plot(mean(v_records(ce+1:ce+ci,:)));
xlabel('Time')
ylabel('Mean Membrane Potential')
title('Mean Cortical Interneuron Membrane Potential')

subplot(3,3,6)
plot(mean(v_records(ce+ci+1:ce+ci+gpi,:)));
xlabel('Time')
ylabel('Mean Membrane Potential')
title('Mean Globus Pallidus Internus Membrane Potential')

%cells firing in each population type per millisecond
subplot(3,3,7)
plot(ce_FR);
xlabel('Time')
ylabel('Firings')
title('Cortical Excitatory Firings')

subplot(3,3,8)
plot(ci_FR);
xlabel('Time')
ylabel('Firings')
title('Cortical Interneuron Firings')

subplot(3,3,9)
plot(gpi_FR);
xlabel('Time')
ylabel('Firings')
title('Globus Pallidus Internus Firings')

dt = 0.001; %size of timestep (seconds = 1; this outputs data in Hz)
Nsamples = 1000; %number of timesteps in entire simulation
hz_ce = (sum(ce_FR(1,1:1000))/ce)/(dt*Nsamples);
hz_ci = (sum(ci_FR(1,1:1000))/ci)/(dt*Nsamples);
hz_gpi = (sum(gpi_FR(1,1:1000))/gpi)/(dt*Nsamples);
hz_total = [hz_ce, hz_ci, hz_gpi];
figure; %mean Hz of each population
bar(1:3,hz_total)
xlabel('1=ce | 2=ci | 3=gpi')
ylabel ('Hz')
title('Average Firing Rate (Hz) of a single neuron in each population')


figure; %firing plot for all populations over time
plot(firings(:,1),firings(:,2),'.');
xlabel('Time')
ylabel('Neuron Number (1-800=ce,801-1000=ci,1001-1200=gpi)')
title('Network Firing over 1000ms')