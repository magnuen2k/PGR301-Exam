# Eksamen PGR301 - maen030

[![Maven-CI](https://github.com/magnuen2k/PGR301-Exam/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/magnuen2k/PGR301-Exam/actions/workflows/maven-ci.yml)
[![Push Image to ECR](https://github.com/magnuen2k/PGR301-Exam/actions/workflows/ecr.yml/badge.svg)](https://github.com/magnuen2k/PGR301-Exam/actions/workflows/ecr.yml)
[![Terraform](https://github.com/magnuen2k/PGR301-Exam/actions/workflows/terraform.yml/badge.svg)](https://github.com/magnuen2k/PGR301-Exam/actions/workflows/terraform.yml)
## Oppgave - Devops

SkalBank sin nåværende utviklingsflyt har utrolig stort forbedringspotensiale. Ved å automatisere ut en del av prosessen, måle/overvåke applikasjonen sin og ha en bedre git flow kan SkalBank få en sikrere og bedre utviklingsopplevelse.

Konfigurer github for bedre flyt i utviklingsprosessen ved hjelp av branch protection:
Naviger til “settings” fanen i ditt repository og velg “branches”. 

![image](https://user-images.githubusercontent.com/53129079/143934317-f779bb4a-fa6f-4ca2-b13c-333e39998668.png)

Her kan man sette opp “rules” for branches. Dette kan f.eks være at det ikke kan merges til “main” med mindre alle tester kjører og koden kompilerer.

Velg “add rule” og fyll inn navnet på branchen man vil protecte.

For å kreve at action workflows kjører før en eventuell merge velger man “Require status checks to pass before merging” og velger hvilke workflows som må fullføres. For å kreve “approval” av andre velger man “Require a pull request before merging” og “Require approvals”. Her kan man velge hvor mange som må godkjenne koden før en eventuell merge kan skje.

Bilde av teksten beskrevet over:

![image](https://user-images.githubusercontent.com/53129079/143934381-8a7a7122-03fb-4a81-ad42-12f20a10b7b0.png)

For å få en effektiv flyt i utviklingen bør utviklere opprette en ny branch for hver oppgave de skal jobbe på, for å så lage en pull request til main igjen. Dersom branch protection er satt opp bra vil dette fungere bra. Branch navn kan f.eks være “feature/<navnpåoppgave>”.

*Jeg har github pro (man får det gratis når man er student), så branch protection fungerte også i private repo for meg.* 

##### Videre drøfting

At banken har valgt å bruke DevOps prinsipper til videre utvikling er et bra og viktig valg. Dette vil automatisere ut en god del av prosessene som per dags dato gjøres manuelt. Fordelingen mellom API-teamet og “Team Dino” vil da være problematisk ettersom “Team Dino” kun er til for å teste systemet. Dersom testerene i “Team Dino” finner noe feil må de rapportere til utviklingsteamet at det er feil i koden, og så må utviklerne finne den for å så sende en ny JAR gjennom Jens. Denne fordelingen skaper utrolig mye kaos uten struktur og mye dødtid ettersom begge teamene sitter å venter på hverandre for å kunne utføre jobben sin. Kontinuerlig integrasjon og skrive tester underveis vil eliminere den dårlige arbeidsfordelingen og gjøre at koden som merges til Main branch ikke har test/kompileringsfeil før det settes i produksjon. En konsekvens av dette vil være at “Team Dino” burde restruktureres for å passe bedre med DevOps prinsipper for team og utvikling, med flere mindre team med god kommunikasjon seg imellom. 

CI/CD automatisering vil åpne opp for muligheter til å gjøre mindre og oftere releases. Dette skaper mindre feil og gjør at det er lettere å gå tilbake hvis noe feil skal oppstå. Feil vil også være lettere å fikser ettersom det ikke vil være en hel release å debugge.

En annen viktig forbedring ved bruk av DevOps prinsipper vil være kommunikasjon. Det at det nå sitter to team og jobber hver for seg og all kommunikasjon skal gå gjennom Jens alene vil være problematisk. F.eks at det overleveres en JAR fil med en hel release på av gangen til testing teamet kan skape kaos hvis de finner feil. Da sitter de to teamene og venter på hverandre for å kunne jobbe, dette skaper “waste”. Noen viktige prinsipper er å bli kvitt waste og eliminere flaskehalser og dette kan gjøres ved at kommunikasjonen mellom utviklere og testere/drift ikke kun går gjennom Jens. Dette gjør at Jens blir såkalt “single point of failure”. Det medfører at Jens ikke kan ta seg ferie i f.eks to uker for da går ikke utviklingen rundt, og det vil skape store problemer dersom Jens blir sykemeldt, ufør eller i verste fall dør.

Infrastruktur som kode er et viktig steg i forbedringen. Dette gjør både at det er lett å holde styr på infrastrukturen, men også at det er lett å replikere den i et testmiljø. Dette er også en del som automatiseres og gjør det lett å gjøre endringer.

Banken har i dag dårlig styr på hvorfor det er så mye feil i applikasjonen og hvor disse feilene oppstår. En viktig forbedring her vil være å utnytte loggeverktøy og konfigurere applikasjonen med telemetri. Ved hjelp at telemetri kan de måle hvor feil oppstår og gjør det lettere i søket etter feilen i koden. Dette er spesielt viktig hos en bank da det vil være lettere å holde applikasjonen i drift fordi de slipper å restarte applikasjonen ofte uten å vite hvor feilen ligger.


## Oppgave - Pipeline

Det står oppgitt i oppgaven at pipeline skal kjøre på hvert *push* mot main. Jeg tolker det slik at den også skal kjøres ved pull request mot main, hvis ikke er litt av poenget med branch protection borte.

Ettersom applikasjonen ofte gir feil vil det være vanskelig å gjøre en ordentlig enhetstest mot servicen. I dette tilfellet vil det være gunstig om testen kjører hver gang for å demonstrerer at pipeline fungerer som det skal, derfor er ikke enhetstesten en “virkelig” service test.
Jeg har likevel lagt ved en test som kunne vært en ordentlig enhetstest, men kommentert ut.

## Oppgave - Feedback

Ettersom vi vet at *ReallyShakyBankingCoreSystemService* kommuniserer med kjernesystemet kan vi ved telemetri i APIet med fakta si i hvilket av systemene problemet oppstår. Dersom servicen kaster en BackendException har noe gått galt i kommunikasjonen til kjernesystemet. Derfor gjør jeg try/catch og catcher eventuelle exceptions som oppstår i servicen, da kan man ved bruk av en counter levere til influx når det oppstår en slik exception. 

Ved bruk av en timer kan jeg måle tiden kommunikasjonen til kjernesystemet bruker. Her ser man at det er utrolig stor forskjell fra request til request og derfor kan man si at kjernesystemet forårsaker de trege responstidene.

#### Spørringer mot Influx for å se telemetri

For å se tiden kjernesystemet har brukt på en request kan man skrive: 

    select * from responseTime where count > 0

![image](https://user-images.githubusercontent.com/53129079/143934468-c670bc69-77af-481a-aeee-2cca110b7df6.png)

Her kan man se hvor lang tid kjernesystemet bruker og hvilken metode det gjelder. Her kan man også se at det ikke er APIet som skaper de trege responstidene.

For å se backend exceptions:

    select * from backendException where value > 0

For å se hvor mange ganger backend exception har skjedd

    select sum(*) from backendException where value > 0

#### Note til influx: 
Det er konfigurert at applikasjonen skal levere data til influx hvert sekund, derfor vil det være mange records med "0" value. Man må gjøre en spørring med en WHERE count/value > 0 for å se recordene som er målt i requesten.

#### Grafana dashboard

Jeg har laget et dashboard hvor man kan se metrics fra de forskjellig endpointene alene eller alle på likt. Det er også en for backend exceptions.

![image](https://user-images.githubusercontent.com/53129079/143936435-cbc7cef3-507e-4756-b312-cc56bb673bc5.png)


#### Litt om logging

I Logback.xml var det konfigurert at det bare skulle logges til console dersom det var en “error”. Jeg endret dette til å være “info” slik at applikasjonen gir fra seg mer logging. Ved bruk av “Logger” klassen kan jeg enkelt logge til console i fra de forskjellige API endpointene.

Man kunne også f.eks konfigurert logging til å være “debug”, men dette ville gitt utrolig mange logger grunnet at applikasjonen hver sekund leverer data til influxdb.

## Oppgave - Terraform

Første gang man kjører terraform kode (uten backend) vil det opprettes en lokal state fil. Dette kan skape problemer når man kjører koden på en annen maskin. Da vil det opprettes en ny state fil og når det skal opprettes som i Skalbank sitt tilfelle en s3 bucket, vil den prøve å opprette en ny fordi den finnes ikke i state filen. Slik får man feilmeldingen med at s3 bucket finnes fra før fordi den prøver å opprette den på nytt. Hadde man hatt en state fil lik for alle (f.eks å ha backend) så ville ikke koden prøve å opprette en ny bucket dersom den lå i state filen. 

Derfor vil dette funke første gang Jens kjører det lokalt hos seg, men ikke hos andre etter han allerede har opprettet denne bucketen. Når han sletter state filen vil ha støte på samme problem som alle andre, forklart over.

#### Opprette en s3 bucket i AWS

For å lage s3 bucket fra CLI, må man først hente nøkler/credentials i AWS. For å gjøre dette i AWS Console må man først gå til servicen “IAM”, velge brukeren man vil hente/lage nøkler for, velge “Security credentials” panelet og trykke på “Create access key”. Her får man bare se sin secret én gang. Derfor er det sterkt anbefalt å lagre denne secret nøkkelen på et sikkert sted.

Neste steg vil være å identifisere seg mot AWS på sin kommandolinje. Dette kan gjøres ved å skrive “aws configure” og paste inn nøklene.

Etter suksessfull identifisering kan man gjøre denne kommandoen for å opprette en s3 bucket på regionen eu-west-1 med navn pgr301-maen030-terraform:

    aws s3api create-bucket --bucket pgr301-maen030-terraform --region eu-west-1 --create-bucket-configuration LocationConstraint=eu-west-1

Terraform kode og pipeline er opprettet og fungerer som det skal. Jeg valgte å lage en branch i mitt eget repo for å teste at terraform kode ikke blir appliet med mindre det merges til main, men det sjekkes at det er gyldig kode.

#### Terraform i pipeline

Når sensor forker dette repo er det viktig at han/hun har nøkler i AWS slik at han/hun kan identifisere seg. I repo må disse nøklene legges inn under “Secrets” hvor access key har navnet “AWS_ACCESS_KEY_ID” og secret har navnet “AWS_SECRET_ACCESS_KEY”.

Slik som på dette bildet:

![image](https://user-images.githubusercontent.com/53129079/143934604-5bf90000-1e39-4c8e-b33f-eeeddeda81be.png)

Dersom sensor skal opprette ny backend for statefil i s3, må sensor først opprette en bucket for dette (f.eks i CLI) og så endre navnet på bucket i backend i provider.tf.

Det samme gjelder for ECR resourcen, dersom sensor skal opprette et nytt ECR repo må det endres til et nytt navn.

Det må også endres i workflowen “ecr.yml” til navnet på ECR repo som sensor har valgt.

## Oppgave - Docker

Vi får oppgitt i oppgaven at applikasjonen, i dockerfilen, skal bygges på jdk-18. Jeg har testet og dette fungerer, men jeg funderer på om dette er lurt ettersom kompileringen gjøres med “maven:3.6-jdk-11”.

*Hva vil kommandolinje for å bygge et container image være?*

    docker build -t maen030/skalbankapi .

*Hva vil kommandolinje for å starte en container være?*

Jeg tar utgangspunkt i at applikasjonen kjører på port 8080.

Starte på port 7777:

    docker run -d -p 7777:8080 --name skalbankapi maen030/skalbankapi

På både port 7777 og 8888:

    docker run -d -p 7777:8080 --name skalbankapi maen030/skalbankapi
    docker run -d -p 8888:8080 --name skalbankapi maen030/skalbankapi

Etter man har laget og startet en container fra et image, kan man stoppe og starte den samme containeren med docker stop/start <navn/id>

## Tilleggsinformasjon

Jeg har valgt å ha workflowene delt opp i egne filer for bedre oversikt under “Actions” fanen i repo.

Jeg har lagt til influxdb.conf i repo ettersom denne må være med for å starte influx.

Dersom man gjør en push direkte til Main vil bygging av docker image og pushing til ECR workflowen kjøre selvom testene brekker. Det vil ikke pushes noe image hvis testene feiler ettersom det er spesifisert i Dockerfilen at koden skal kompileres. Uansett, så er det viktig med branch protection som gjør at Main krever en PR hvor tester kjører for å kunne merge.

Kandidatnr:

    2014
