# proiectAWBD
Sistem de Programari Medicale

Sistemul de management medical utilizează un model relațional complex format din 11 entități interconectate. Arhitectura bazei de date a fost concepută pentru a gestiona fluxul complet al unei clinici: de la autentificare și profiluri de utilizatori (Doctori/Pacienți), până la logica de programări, plăți și scheme de asigurare/abonament.

Relații @OneToOne
User => Patient / Doctor: Fiecare User este legat de un singur profil clinic. Acest lucru permite separarea datelor de autentificare de datele medicale.
Appointment => Payment: O programare are asociată o singură tranzacție financiară, asigurând trasabilitatea plăților.

Relații @ManyToOne/@OneToMany 
Doctor => MedicalService: Mai mulți doctori pot fi specializați pe același serviciu medical.
Patient => Appointment: Un pacient poate avea mai multe programări de-a lungul timpului.
Doctor => PaidTimeOff: Un doctor poate solicita mai multe perioade de concediu.
InsuranceProvider => Patient: Mai mulți pacienți pot fi arondați aceluiași furnizor de asigurări medicale.

Relații @ManyToMany
MedicalService => InsuranceProvider: Implementată prin tabelul de joncțiune SERVICE_INSURANCE_COVERAGE. Un serviciu medical poate fi acoperit de mai mulți asiguratori, iar un asigurator poate acoperi o gamă largă de servicii.
MedicalService => SubscriptionPlan: Implementată prin SERVICE_SUBSCRIPTION_PLAN. Modelează ofertele de tip abonament unde un plan include mai multe servicii, iar un serviciu poate face parte din pachete diferite.

Diagrama ER
<img width="967" height="653" alt="image" src="https://github.com/user-attachments/assets/6bfe2178-60d7-4190-9c50-237f974c9171" />

Diagrama conceptuala 
<img width="975" height="970" alt="image" src="https://github.com/user-attachments/assets/3e452899-61fa-4d6d-b37c-047688b51320" />


