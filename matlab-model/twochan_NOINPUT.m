% based on work by Eugene M. Izhikevich, February 25, 2003
% some parameters based on Thibeault & Srinivasa, 2013
% cortical/basal ganglia networks of Izhikevich neurons by Nick Spielman,
% Shuo Yang, Jadin C. Jackson

NB=1000; % desired size of cortical network
NC=1000; % desired size of basal ganglia network
ts=1000; % desired timestep of simulation
channels = 2; % desired number of channels
f_factor = 4; % fudge factor for tiny populations (GPe,GPi, STN)

CE=floor(0.8*NC); % 800 Excitatory neurons
CI=ceil(0.2*NC);  % 200 Inhibitory neurons
TC=ceil(0.01014*NB); % 11 ThalamoCortical neurons
STN=ceil(0.00468*NB)*f_factor; % 20 Subthalamic nucleus neurons
                     %STN=ceil(0.0468*NB)*f_factor; % 20 Subthalamic nucleus neurons
strD1=floor(0.47961*NB)*2; % 479*2 = 958 strD1
strD2=floor(0.47961*NB); % 479 strD2
GPe=floor(0.01582*NB)*f_factor; % 60 GPe
GPi=ceil(0.01014*NB)*f_factor; % 44 GPi
ALL = CE+CI+TC+STN+strD1+strD2+GPe+GPi; % 2572

% indices for each type of neurons (populations),
% _1: channel 1; _2: channel 2
iCE_1=[1:CE]; % 800 * 1
iCE_2=[ALL+1:ALL+CE];

iCI_1=[(CE+1):(CE+CI)];
iCI_2=[(ALL+CE+1):(ALL+CE+CI)];

iTC_1=[(CE+CI+1):(CE+CI+TC)];
iTC_2=[(ALL+CE+CI+1):(ALL+CE+CI+TC)];

iSTN_1=[(CE+CI+TC+1):(CE+CI+TC+STN)];
iSTN_2=[(ALL+CE+CI+TC+1):(ALL+CE+CI+TC+STN)];

istrD1_1=[(CE+CI+TC+STN+1):(CE+CI+TC+STN+strD1)];
istrD1_2=[(ALL+CE+CI+TC+STN+1):(ALL+CE+CI+TC+STN+strD1)];

istrD2_1=[(CE+CI+TC+STN+strD1+1):(CE+CI+TC+STN+strD1+strD2)];
istrD2_2=[(ALL+CE+CI+TC+STN+strD1+1):(ALL+CE+CI+TC+STN+strD1+strD2)];

iGPe_1=[(CE+CI+TC+STN+strD1+strD2+1):(CE+CI+TC+STN+strD1+strD2+GPe)];
iGPe_2=[(ALL+CI+TC+STN+strD1+strD2+1):(ALL+CE+CI+TC+STN+strD1+strD2+GPe)];

iGPi_1=[(CE+CI+TC+STN+strD1+strD2+GPe+1):(CE+CI+TC+STN+strD1+strD2+GPe+GPi)];
iGPi_2=[(ALL+CE+CI+TC+STN+strD1+strD2+GPe+1):(ALL+CE+CI+TC+STN+strD1+strD2+GPe+GPi)];

ich1=[1:ALL];
ich2=[ALL+1:(ALL*2)];

CE_a=0.02*ones(CE,1); 
CE_b=0.2*ones(CE,1);               
CE_c=-65+15*(rand(CE,1).^2); 
CE_d=8-6*(rand(CE,1).^2);
CE_I=3.75;
pCE2CECI=(1000/NC); %default connection probability at NC=1000 is 1; 0.0021429
pCE2STN=(250/NC);
pCE2strD12=(500/NC);
CE_S=[(0.5*rand(CE+CI,CE).*double(rand(CE+CI,CE)<pCE2CECI)); ...
      zeros(TC,CE); (0.25*rand(STN,CE).*double(rand(STN,CE)<pCE2STN));
      (0.2*rand(strD1+strD2,CE).*double(rand(strD1+strD2,CE)<pCE2strD12));
      zeros(GPe+GPi,CE)]; % ALL * CE (2572 * 800)

CI_a=0.2+0.08*rand(CI,1); 
CI_b=0.25-0.05*rand(CI,1);               
CI_c=-65*ones(CI,1); 
CI_d=2*(rand(CI,1).^2);    
CI_I=2;
pCI2CICE=1000/NC;
CI_S=[(-rand(CE+CI,CI).*double(rand(CE+CI,CI)<pCI2CICE));
      zeros(TC+STN+strD1+strD2+GPe+GPi,CI)]; % ALL * CI 

%thalamocortical population
TC_a=0.002*ones(TC,1); 
TC_b=0.25*ones(TC,1);         
TC_c=-65+15*(rand(TC,1).^2); 
TC_d=0.05*(rand(TC,1).^2);
TC_S=[zeros(ALL,TC)]; % ALL * TC

STN_a=0.005*ones(STN,1); 
STN_b=0.265*ones(STN,1); 
STN_c=-65+15*(rand(STN,1).^2); 
STN_d=2*(rand(STN,1).^2); 
STN_I=0.5;
pSTN2STN=250/NB;
pSTN2GPe=250/NB;
pSTN2GPi=800/NB;
STN_S=[zeros(CE+CI+TC,STN);(0.1*rand(STN,STN).*double(rand(STN, ...
                                                  STN)<pSTN2STN));
       zeros(strD1+strD2,STN);(0.2*rand(GPe,STN).*double(rand(GPe, ...
                                                  STN)<pSTN2GPe));
       (0.2*rand(GPi,STN).*double(rand(GPi,STN)<pSTN2GPi))]; % ALL*STN

strD1_a=0.01+0.01*rand(strD1,1); 
strD1_b=0.275-0.05*rand(strD1,1); 
strD1_c=-65*ones(strD1,1); 
strD1_d=2*ones(strD1,1);
pstrD12strD1=750/NB;
pstrD12GPi=1000/NB;
strD1_S=[zeros(CE+CI+TC+STN,strD1);
         (-rand(strD1,strD1).*double(rand(strD1,strD1)< ...
                                     pstrD12strD1));
         zeros(strD2+GPe,strD1);(-rand(GPi,strD1).*double(rand(GPi,strD1)<pstrD12GPi))];


%above parameters are for bistable striatal neurons; Thibeault & Srinivasa
%state bistability not necessary for action selection model, but using
%these would be really cool...would need to modify cortical input
strD2_a=0.01+0.01*rand(strD2,1); 
strD2_b=0.275-0.05*rand(strD2,1); 
strD2_c=-65*ones(strD2,1); 
strD2_d=2*ones(strD2,1);
pstrD22strD2=750/NB;
pstrD22GPe=1000/NB;
strD2_S=[zeros(CE+CI+TC+STN+strD1,strD2);
         (-rand(strD2,strD2).*double(rand(strD2,strD2)< ...
                                     pstrD22strD2));
         (-2*rand(GPe,strD2).*double(rand(GPe,strD2)<pstrD22GPe));
         zeros(GPi,strD2)];

GPe_a=0.005+0.001*rand(GPe,1);
GPe_b=0.585-0.05*rand(GPe,1); 
GPe_c=-65*ones(GPe,1); 
GPe_d=4*ones(GPe,1);
pGPe2STN=800/NB;
pGPe2GPe=750/NB;
GPe_S=[zeros(CE+CI+TC,GPe);(-rand(STN,GPe).*double(rand(STN,GPe)< ...
                                                  pGPe2STN));
       zeros(strD1+strD2,GPe);(-5*rand(GPe,GPe).*double(rand(GPe,GPe)<pGPe2GPe));zeros(GPi,GPe)];
GPe_I=0;

GPi_a=0.005+0.001*rand(GPi,1); 
GPi_b=0.32-0.05*rand(GPi,1); 
GPi_c=-65*ones(GPi,1); 
GPi_d=2*ones(GPi,1);
pGPi2GPi=500/NB;
pGPi2TC=1000/NB;
GPi_S=[zeros(CE+CI,GPi);(-rand(TC,GPi).*double(rand(TC,GPi)< ...
                                               pGPi2TC));
       zeros(STN+strD1+strD2+GPe,GPi);(-0.5*rand(GPi,GPi).*double(rand(GPi,GPi)<pGPi2GPi))];
GPi_I=15;

a=[CE_a; CI_a; TC_a; STN_a; strD1_a; strD2_a; GPe_a; GPi_a;];
b=[CE_b; CI_b; TC_b; STN_b; strD1_b; strD2_b; GPe_b; GPi_b;];
c=[CE_c; CI_c; TC_c; STN_c; strD1_c; strD2_c; GPe_c; GPi_c;];
d=[CE_d; CI_d; TC_d; STN_d; strD1_d; strD2_d; GPe_d; GPi_d;];
S=[CE_S,CI_S,TC_S,STN_S,strD1_S,strD2_S,GPe_S,GPi_S];
v=-65*ones(CE+CI+TC+STN+strD1+strD2+GPe+GPi,1); % Initial values of v

if channels > 1
    % make a block of matrix with the
    % dimension of channels * 1, each element is 'a'.
    a = repmat(a,channels,1); % 5144 * 1
    b = repmat(b,channels,1);
    c = repmat(c,channels,1);
    d = repmat(d,channels,1);
    v = repmat(v,channels,1);
    
    % "diffuse projections listed in Table 1B, spanned all 
    % channels and the connection probability was divided 
    % among each of those" - Thibeault & Srinivasa, 2013
    pSTN_diffuse = 0.5/channels; 

    % diffuse connections of the STN, 2572 * 20, only affects GPe
    % and GPi.
    STN_S_diffuse = [zeros(CE+CI+TC+STN+strD1+strD2,STN);
                     (0.35*rand(GPe,STN).*double(rand(GPe,STN)< ...
                                                 pSTN_diffuse));
                     (0.35*rand(GPi,STN).*double(rand(GPi,STN)<pSTN_diffuse))];

    % outgoing synaptic connections of the new channel, 2572 * 2572
    S_new = [zeros(ALL,CE+CI+TC),STN_S_diffuse,zeros(ALL,strD1+strD2+GPe+GPi)];    

    % 5144 * 5144
    S_empty = repmat(S_new,channels,channels);
    S_empty = [S,S_new;S_new,S];
    S = S_empty;
else
end;
sparse(S);

figure;
imagesc(S);
xlabel('Neuronal Input')
ylabel('Current Neuron')
title('Synaptic weight matrix')
return

u=b.*v; % Initial values of u
firings=[]; % spike timings

%%
tic
for t=1:ts % simulation of ts milliseconds
    
    % 5144 * 1, thalamic input
    I=[CE_I*randn(CE,1);CI_I*randn(CI,1);zeros(TC,1);
       STN_I*randn(STN,1);zeros(strD1+strD2,1)
       ;GPe_I*randn(GPe,1);GPi_I*randn(GPi,1);
       CE_I*randn(CE,1);CI_I*randn(CI,1);zeros(TC,1);
       STN_I*randn(STN,1);zeros(strD1+strD2,1);
       GPe_I*randn(GPe,1);GPi_I*randn(GPi,1)];
      
    fired=find(v>=30); % indices of spikes
    firings=[firings; t+0*fired,fired];
    v(fired)=c(fired);
    u(fired)=u(fired)+d(fired);
    I=I+sum(S(:,fired),2);
    v=v+0.5*(0.04*v.^2+5*v+140-u+I); % step 0.5 ms
    v=v+0.5*(0.04*v.^2+5*v+140-u+I); % for numerical
    u=u+a.*(b.*v-u); % stability
end;
toc
time=toc;

%%

plot(firings(:,1),firings(:,2),'.');

% id, time
spfirings = sparse(firings(:,2),firings(:,1),1);

ch1GPe_fr=(sum(spfirings(iGPe_1,:),1)/GPe);
ch2GPe_fr=(sum(spfirings(iGPe_2,:),1)/GPe);


plot(1:ts,ch1GPe_fr,'b-',1:ts,ch2GPe_fr,'r-')

%%

CE_1Hz=((sum(sum(spfirings(iCE_1,:)))/CE)/ts)*1000;
CE_2Hz=((sum(sum(spfirings(iCE_2,:)))/CE)/ts)*1000;


CI_1Hz=((sum(sum(spfirings(iCI_1,:)))/CI)/ts)*1000;
CI_2Hz=((sum(sum(spfirings(iCI_2,:)))/CI)/ts)*1000;


STN_1Hz=((sum(sum(spfirings(iSTN_1,:)))/STN)/ts)*1000;
STN_2Hz=((sum(sum(spfirings(iSTN_2,:)))/STN)/ts)*1000;


strD1_1Hz=((sum(sum(spfirings(istrD1_1,:)))/strD1)/ts)*1000;
strD1_2Hz=((sum(sum(spfirings(istrD1_2,:)))/strD1)/ts)*1000;


strD2_1Hz=((sum(sum(spfirings(istrD2_1,:)))/strD2)/ts)*1000;
strD2_2Hz=((sum(sum(spfirings(istrD2_2,:)))/strD2)/ts)*1000;


GPe_1Hz=((sum(sum(spfirings(iGPe_1,:)))/GPe)/ts)*1000;
GPe_2Hz=((sum(sum(spfirings(iGPe_2,:)))/GPe)/ts)*1000;


GPi_1Hz=((sum(sum(spfirings(iGPi_1,:)))/GPi)/ts)*1000;
GPi_2Hz=((sum(sum(spfirings(iGPi_2,:)))/GPi)/ts)*1000;


log = [CE_1Hz, CE_2Hz; CI_1Hz, CI_2Hz; STN_1Hz, STN_2Hz;...
       strD1_1Hz, strD1_2Hz; strD2_1Hz, strD2_2Hz;...
       GPe_1Hz, GPe_2Hz; GPi_1Hz, GPi_2Hz];