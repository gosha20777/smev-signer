# smev-client
a rest-api client for smev3

## roadmap
 - [X] disassemble and get the source code of the latest smv3 adapter
 - [X] implement the xml normalization algorithm according to the urn normalization algoritm `urn://smev-gov-ru/xmldsig/transform`
 - [ ] add all mesages types support:
   - [X] AckRequest
   - [X] GetResponseRequest
   - [X] SendRequestRequest
   - [ ] GetResponseResponse
   - [ ] SendRequestResponse
 - [X] implement a console application for test
 - [X] add openensl + gost engine support
    - [X] test it
 - [ ] rewrite servise to python
   - [ ] add rest api support
   - [ ] docker support
   - [ ] write user guide and docs
   - [ ] remove java code

### key objectives of the project and worfflow concept
 - open source
 - free
 - sipmle
 - esy to use & install
 - RESTfull api
 - fast
 - docker compatible
 - built on open technologies and *cryptopro independent*

```json
===>
POST http://host/schedule_query/FNSVipUL/
{
   "ЗапросЮЛ": {
"ОГРН": "1027700070518"
},
"ИдДок": "3"
}

<===
Task id
```

```json
===>
http://localhost:9003/get_document/FNSVipUL/1027700070518

{
    "status": "sucsess",
    "message": "сообщение отправлено в смэв"
}
```

*work in porgress*