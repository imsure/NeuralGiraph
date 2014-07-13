% based on work by Eugene M. Izhikevich, February 25, 2003
% some parameters based on Thibeault & Srinivasa, 2013
% cortical/basal ganglia networks of Izhikevich neurons by Nick Spielman,
% Shuo Yang, Jadin C. Jackson

NB=1000;%desired size of cortical network
NC=1000;%desired size of basal ganglia network
ts=8000;%desired timestep of simulation
channels = 3;%desired number of channels
sel_channel = 2;%channel to be selected
sel_strength = 5;%strength of input current to selected channel

CE=floor(0.8*NC);
CI=ceil(0.2*NC);
GPi=ceil(0.01014*NB);
TC=ceil(0.01014*NB);  
STN=ceil(0.00468*NB); 
strD1=floor(0.47961*NB); 
strD2=floor(0.47961*NB);
GPe=floor(0.01582*NB); 

ALL=CE+CI+TC+STN+strD1+strD2+GPe+GPi;

CE_a=0.02*ones(CE,1); 
CE_b=0.2*ones(CE,1);               
CE_c=-65+15*(rand(CE,1).^2); 
CE_d=8-6*(rand(CE,1).^2);    
CE_I=3.75;
pCE2CECI=(1000/NC); %default connection probability at NC=1000 is 1; 0.0021429
pCE2STN=(250/NC);
pCE2strD12=(500/NC);
CE_S=[(0.5*rand(CE+CI,CE).*double(rand(CE+CI,CE)<pCE2CECI));zeros(TC,CE);(0.25*rand(STN,CE).*double(rand(STN,CE)<pCE2STN));(0.5*rand(strD1+strD2,CE).*double(rand(strD1+strD2,CE)<pCE2strD12));zeros(GPe+GPi,CE)];

CI_a=0.2+0.08*rand(CI,1); 
CI_b=0.25-0.05*rand(CI,1);               
CI_c=-65*ones(CI,1); 
CI_d=2*(rand(CI,1).^2);    
CI_I=2;
pCI2CICE=1000/NC;
CI_S=[(-rand(CE+CI,CI).*double(rand(CE+CI,CI)<pCI2CICE));zeros(TC+STN+strD1+strD2+GPe+GPi,CI)];

                                                   %thalamocortical population
TC_a=0.002*ones(TC,1); 
TC_b=0.25*ones(TC,1);               %TC a and b parameters
TC_c=-65+15*(rand(TC,1).^2); 
TC_d=0.05*(rand(TC,1).^2);    %TC c and d parameters
%TC_I=0; applied current (I) for TC neurons not indicated in Thibeault &
%Srinavasa, 2013
TC_S=[zeros(ALL,TC)];


STN_a=0.005*ones(STN,1); 
STN_b=0.265*ones(STN,1); 
STN_c=-65+15*(rand(STN,1).^2); 
STN_d=2*(rand(STN,1).^2); 
STN_I=0.5;
pSTN2STN=250/NB;
pSTN2GPi=800/NB;
STN_S=[zeros(CE+CI+TC,STN);(0.1*rand(STN,STN).*double(rand(STN,STN)<pSTN2STN));zeros(strD1+strD2+GPe,STN);(0.2*rand(GPi,STN).*double(rand(GPi,STN)<pSTN2GPi))];
%S=[-1*rand(Ni,Ni).*double(rand(Ni,Ni)<PgpeTI2gpeTI) ];

%strD1_a=0.01*rand(strD1,1); 
%strD1_b=0.25-20*rand(strD1,1); 
%strD1_c=-80*ones(strD1,1); 
%strD1_d=150*ones(strD1,1);
strD1_a=0.01+0.01*rand(strD1,1); 
strD1_b=0.275-0.05*rand(strD1,1); 
strD1_c=-65*ones(strD1,1); 
strD1_d=2*ones(strD1,1);
pstrD12strD1=750/NB;
pstrD12GPi=1000/NB;
strD1_S=[zeros(CE+CI+TC+STN,strD1);(-2*rand(strD1,strD1).*double(rand(strD1,strD1)<pstrD12strD1));zeros(strD2+GPe,strD1);(-rand(GPi,strD1).*double(rand(GPi,strD1)<pstrD12GPi))];
%strD1_I=0;

 
%strD2_a=0.01*rand(strD2,1); 
%strD2_b=0.25-20*rand(strD2,1); 
%strD2_c=-80*ones(strD2,1); 
%strD2_d=150*ones(strD2,1);
%above parameters are for bistable striatal neurons; Thibeault & Srinivasa
%state bistability not necessary for action selection model, but using
%these would be really cool...would need to modify cortical input
strD2_a=0.01+0.01*rand(strD2,1); 
strD2_b=0.275-0.05*rand(strD2,1); 
strD2_c=-65*ones(strD2,1); 
strD2_d=2*ones(strD2,1);
pstrD22strD2=750/NB;
pstrD22GPe=1000/NB;
strD2_S=[zeros(CE+CI+TC+STN+strD1,strD2);(-2*rand(strD2,strD2).*double(rand(strD2,strD2)<pstrD22strD2));(-2*rand(GPe,strD2).*double(rand(GPe,strD2)<pstrD22GPe));zeros(GPi,strD2)];
%strD2_I=0;


GPe_a=0.005+0.001*rand(GPe,1); %Thibeault and Srinivasa state "0.005" for GPe, rand matrix added to conserve dynamic heterogeneity 
GPe_b=0.585-0.05*rand(GPe,1); 
GPe_c=-65*ones(GPe,1); 
GPe_d=4*ones(GPe,1);
pGPe2STN=800/NB;
pGPe2GPe=750/NB;
GPe_S=[zeros(CE+CI+TC,GPe);(-rand(STN,GPe).*double(rand(STN,GPe)<pGPe2STN));zeros(strD1+strD2,GPe);(-2*rand(GPe,GPe).*double(rand(GPe,GPe)<pGPe2GPe));zeros(GPi,GPe)];
GPe_I=0;

 
GPi_a=0.005+0.001*rand(GPi,1); 
GPi_b=0.32-0.05*rand(GPi,1); 
GPi_c=-65*ones(GPi,1); 
GPi_d=2*ones(GPi,1);
pGPi2GPi=500/NB;
pGPi2TC=1000/NB;
GPi_S=[zeros(CE+CI,GPi);(-rand(TC,GPi).*double(rand(TC,GPi)<pGPi2TC));zeros(STN+strD1+strD2+GPe,GPi);(-0.5*rand(GPi,GPi).*double(rand(GPi,GPi)<pGPi2GPi))];
GPi_I=5;

 
%SNr_a=0.005+0.08*rand(SNr,1); SNr_b=0.32-0.05*rand(SNr,1); 
%SNr_c=-65*ones(SNr,1); SNr_d=2*ones(SNr,1);
%SNr_I=25;

a=[CE_a; CI_a; TC_a; STN_a; strD1_a; strD2_a; GPe_a; GPi_a;];
b=[CE_b; CI_b; TC_b; STN_b; strD1_b; strD2_b; GPe_b; GPi_b;];
c=[CE_c; CI_c; TC_c; STN_c; strD1_c; strD2_c; GPe_c; GPi_c;];
d=[CE_d; CI_d; TC_d; STN_d; strD1_d; strD2_d; GPe_d; GPi_d;];
S=[CE_S,CI_S,TC_S,STN_S,strD1_S,strD2_S,GPe_S,GPi_S];
v=-65*ones(CE+CI+TC+STN+strD1+strD2+GPe+GPi,1); % Initial values of v

if channels > 1

a = repmat(a,channels,1);
b = repmat(b,channels,1);
c = repmat(c,channels,1);
d = repmat(d,channels,1);
v = repmat(v,channels,1);

pSTN_diffuse = 0.25/channels; % "diffuse projections listed in Table 1B, spanned all channels and the connection probability was divided among each of those" - Thibeault & Srinivasa, 2013

STN_S_diffuse = [zeros(CE+CI+TC+STN+strD1+strD2,STN);(0.35*rand(GPe,STN).*double(rand(GPe,STN)<pSTN_diffuse));(0.35*rand(GPi,STN).*double(rand(GPi,STN)<pSTN_diffuse))];%diffuse connections of the STN

S_new = [zeros(ALL,CE+CI+TC),STN_S_diffuse,zeros(ALL,strD1+strD2+GPe+GPi)];%outgoing synaptic connections of the new channel
    
S_empty = repmat(S_new,channels,channels);

for i=0:(channels-1) %

    S_empty((ALL*i)+1:ALL*(i+1),(ALL*i)+1:ALL*(i+1))=S;
    
end;

S = S_empty;

else
end;
sparse(S);

figure;
image(S);
xlabel('Neuronal Input')
ylabel('Current Neuron')
title('Synaptic weight matrix')

u=b.*v; % Initial values of u
firings=[]; % spike timings


tic
for t=1:ts % simulation of ts milliseconds

I=[CE_I*randn(CE,1);CI_I*randn(CI,1);zeros(TC,1);STN_I*randn(STN,1);zeros(strD1+strD2,1);GPe_I*randn(GPe,1);GPi_I*randn(GPi,1);]; % thalamic input
    
for j=0:(channels-2) %duplicates I * number of channels (DOES rerandomize for each new channel)
I=[I;CE_I*randn(CE,1);CI_I*randn(CI,1);zeros(TC,1);STN_I*randn(STN,1);zeros(strD1+strD2,1);GPe_I*randn(GPe,1);GPi_I*randn(GPi,1);];
end;

if t > 1000
I(1:CE,1) = 9.2*randn; %apply input to CE to generate 20 Hz input to channel 1
end;

if t > 2500
I(ALL+1:(ALL*2),1) = 13*randn; %apply input to CE to generate 30 Hz input to channel 2
end;

if t > 5000
I(1+(2*ALL):(3*ALL),1) = 17.5*randn; %apply input to CE to generate 40 Hz input to channel 3
end;

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
plot(firings(:,1),firings(:,2),'.');

spfirings = sparse(firings(:,2),firings(:,1),1);

ch1_fr=sum(spfirings(1:ALL,:),1)/(1000/ALL);
ch2_fr=sum(spfirings(ALL+1:ALL*2,:),1)/(1000/ALL);
ch3_fr=sum(spfirings((ALL*2)+1:ALL*3,:),1)/(1000/ALL);
plot(1:ts,ch1_fr,'b-',1:ts,ch2_fr,'r-',1:ts,ch3_fr,'y-')




avgFR = nan(ALL,1);
for iN=1:ALL
    avgFR(iN) = (sum(firings(:,2)==iN))/(ts/ALL);
end
avgtotal=mean(avgFR);
stdtotal=std(avgFR);
Fanototal = stdtotal^2/avgtotal;

CEavg=mean(avgFR(1:CE));
CEstd=std(avgFR(1:CE));
CEFano=CEstd^2/CEavg;

CIavg=mean(avgFR(CE+1:CE+CI));
CIstd=std(avgFR(CE+1:CE+CI));
CIFano=CIstd^2/CIavg;

TCavg=mean(avgFR(CE+CI+1:CE+CI+TC));
TCstd=std(avgFR(CE+CI+1:CE+CI+TC));
TCFano=TCstd^2/TCavg;

STNavg=mean(avgFR(CE+CI+TC+1:CE+CI+TC+STN));
STNstd=std(avgFR(CE+CI+TC+1:CE+CI+TC+STN));
STNFano=STNstd^2/STNavg;

strD1avg=mean(avgFR(CE+CI+TC+STN+1:CE+CI+TC+STN+strD1));
strD1std=std(avgFR(CE+CI+TC+STN+1:CE+CI+TC+STN+strD1));
strD1Fano=strD1std^2/strD1avg;

strD2avg=mean(avgFR(CE+CI+TC+STN+strD1+1:CE+CI+TC+STN+strD1+strD2));
strD2std=std(avgFR(CE+CI+TC+STN+strD1+1:CE+CI+TC+STN+strD1+strD2));
strD2Fano=strD2std^2/strD2avg;

GPeavg=mean(avgFR(CE+CI+TC+STN+strD1+strD2+1:CE+CI+TC+STN+strD1+strD2+GPe));
GPestd=std(avgFR(CE+CI+TC+STN+strD1+strD2+1:CE+CI+TC+STN+strD1+strD2+GPe));
GPeFano=GPestd^2/GPeavg;

GPiavg=mean(avgFR(CE+CI+TC+STN+strD1+strD2+GPe+1:CE+CI+TC+STN+strD1+strD2+GPe+GPi));
GPistd=std(avgFR(CE+CI+TC+STN+strD1+strD2+GPe+1:CE+CI+TC+STN+strD1+strD2+GPe+GPi));
GPiFano=GPistd^2/GPiavg;

log=[avgtotal,stdtotal,Fanototal,CEavg,CEstd,CEFano,CIavg,CIstd,CIFano,TCavg,TCstd,TCFano,STNavg,STNstd,STNFano,strD1avg,strD1std,strD1Fano,strD2avg,strD2std,strD2Fano,GPeavg,GPestd,GPeFano,GPiavg,GPistd,GPiFano];





%text(1000/2, 500, num2str(avgtotal),'FontSize',40)

%avgFR = nan(1000,1);
%isi =[];
%for iN=1:1000
%    avgFR(iN) = (sum(firings(:,2)==iN))/(1000/1000); 
%    isi = [isi; diff(firings(firings(:,2)==iN,1))];
%end
%avgtotal=mean(avgFR);
%stdtotal=std(avgFR);
%Fano = stdtotal^2/avgtotal;
%text(1000/2, 500, num2str(avgtotal),'FontSize',40)
%avgisi = mean(isi);
%stdisi = std(isi);
%figure;
%hist(isi,[1:1000]);

