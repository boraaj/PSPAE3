# PSPAE3
PROJECTE PSPAE3 
Deselvolupat pero Borja Zafra y Pablo Rozalén. 

Aspectes de importancia abans de ejecutar. 

1. BBDD MongoDB => Necessitem crear una BBDD MongoDB a MongoDb Compass Anomenada "policia" i una coleccion anomenada "delincuentes"
2. Importar document de proba a MongoDB Compass desde el arxiu DocumentProba.json a la branca Main. 

URL De Peticions 
 
 // GET // 
 - Mostrar totes els alies de delincuents => http://localhost:7777/servidor/mostrarTodos
 - Mostrar dades de un delincuent => http://localhost:7777/servidor/mostrarUno?=(alies de delincuent);
 
 // POST //
 
 Dins de POSTMAN 
 - Afegir una nova peticio POST y afegir al body el json del document DOcumentProba.json. 
   IMPORTANT! Al fer la peticio, el POSTMAN es quedara procesant fins que acaben de introduir les dades del mail a la consola de Eclipse. 
   
   URL de peticio => http://localhost:7777/servidor/nuevo
   
 - Al introduir les dades del mail en la consola de Eclipse ens demanara, entre altres, la contrasenya del mail. Aquesta contrasenya es la contrasenya generada per GMail    que ens proporciona al afegir autoritzacio pero emprar una aplicacion externa. No la contrasenya que tenim de eixe compte de Mail!!!
 
  (; Gracies per la atencio ;)
