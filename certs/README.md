# Certificate signatures
This information has been extracted using the command:
```
$ keytool -list -v -keystore <keystore_file>
```

## Debug

```
Tipo de Almacén de Claves: JKS
Proveedor de Almacén de Claves: SUN

Su almacén de claves contiene 1 entrada

Nombre de Alias: androiddebugkey
Fecha de Creación: 19-oct-2015
Tipo de Entrada: PrivateKeyEntry
Longitud de la Cadena de Certificado: 1
Certificado[1]:
Propietario: CN=Android Debug, O=Android, C=US
Emisor: CN=Android Debug, O=Android, C=US
Número de serie: 3c71b5c1
Válido desde: Mon Oct 19 11:29:36 CEST 2015 hasta: Wed Oct 11 11:29:36 CEST 2045
Huellas digitales del Certificado:
	 MD5: A1:BC:FB:90:A4:D4:21:C8:4A:5E:29:4C:C2:4E:5E:CF
	 SHA1: 8D:7D:74:53:A4:7A:BA:CA:1A:FA:6D:5E:D1:BB:A7:67:92:C8:41:12
	 SHA256: ED:05:68:1E:0F:BC:59:14:C7:28:28:AF:EB:DD:5A:B9:8F:7A:43:33:68:EA:96:29:65:14:E4:35:CA:46:2C:B5
	 Nombre del Algoritmo de Firma: SHA256withRSA
	 Versión: 3

Extensiones:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: 3A C5 6A D2 3F 98 58 81   C6 0C 50 62 A3 25 20 D5  :.j.?.X...Pb.% .
0010: 54 49 48 65                                        TIHe
]
]
```