# Certificate signatures
This information has been extracted using the command:
```
$ keytool -list -v -keystore <keystore_file>
```

## Fake release

```
Tipo de Almacén de Claves: PKCS12
Proveedor de Almacén de Claves: SUN

Su almacén de claves contiene 1 entrada

Nombre de Alias: caducity
Fecha de Creación: 5 ene 2026
Tipo de Entrada: PrivateKeyEntry
Longitud de la Cadena de Certificado: 1
Certificado[1]:
Propietario: CN=Bernat Borras Paronella
Emisor: CN=Bernat Borras Paronella
Número de serie: c3a76ca466e5d250
Válido desde: Mon Jan 05 11:20:12 CET 2026 hasta: Fri May 23 12:20:12 CEST 2053
Huellas digitales del certificado:
         SHA1: 8F:0A:FF:5E:99:19:F5:8F:19:79:04:59:02:88:1B:D3:B6:D8:9E:CE
         SHA256: D0:7A:A4:8E:FF:D3:37:67:4A:74:F4:59:DE:17:2C:28:91:72:1F:E8:68:1F:CB:F1:32:2F:46:78:34:E7:1C:4F
Nombre del algoritmo de firma: SHA384withRSA
Algoritmo de clave pública de asunto: Clave RSA de 4096 bits
Versión: 3

Extensiones: 

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: 8E 69 E1 69 03 C6 5D F8   D5 6F E6 2D AC F4 8B 3A  .i.i..]..o.-...:
0010: 0D CF D6 90                                        ....
]
]
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