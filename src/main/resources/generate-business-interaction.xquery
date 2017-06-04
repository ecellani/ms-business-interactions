<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cus="http://www.gvt.com.br/CustomerManagement/CustomerSelfManagement/CustomerSelfEmpowered/CustomerSelfEmpowered/" xmlns:gen="http://www.gvt.com.br/CustomerManagement/CustomerSelfManagement/CustomerSelfEmpowered/CustomerSelfEmpowered/generateBusinessInteractionEntities">
   <soapenv:Header/>
   <soapenv:Body>
      <cus:generateBusinessInteractionIn>
         <gen:serviceRequestType>
            <gen:id>{data(/sear/id)}</gen:id>
            <gen:serviceId>{data(/sear/serviceId)}</gen:serviceId>
            <gen:channel>{data(/sear/channel)}</gen:channel>
         </gen:serviceRequestType>
      </cus:generateBusinessInteractionIn>
   </soapenv:Body>
</soapenv:Envelope>
