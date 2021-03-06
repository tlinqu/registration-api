package gov.samhsa.c2s.patientregistration.service.util;

import gov.samhsa.c2s.patientregistration.config.IdentifierProperties;
import gov.samhsa.c2s.patientregistration.service.dto.SignupDto;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;

public class FhirResourceConverter {

    @Autowired
    private IdentifierProperties identifierProperties;



    private Function<String, Enumerations.AdministrativeGender> getPatientGender = new Function<String, AdministrativeGender>() {
        @Override
        public AdministrativeGender apply(String codeString) {
            if (codeString != null && !"".equals(codeString) || codeString != null && !"".equals(codeString)) {
                if ("male".equalsIgnoreCase(codeString) || "M".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.MALE;
                } else if ("female".equalsIgnoreCase(codeString) || "F".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.FEMALE;
                } else if ("other".equalsIgnoreCase(codeString) || "O".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.OTHER;
                } else if ("unknown".equalsIgnoreCase(codeString) || "UN".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.UNKNOWN;
                } else {
                    throw new IllegalArgumentException("Unknown AdministrativeGender code \'" + codeString + "\'");
                }
            } else {
                return null;
            }
        }
    };
    private Function<SignupDto, Patient> signupDtoToPatient = new Function<SignupDto, Patient>() {
        @Override
        public Patient apply(SignupDto signupDto) {
            Patient patient = new Patient();
            //setting mandatory fields
            patient.setId(new IdType(signupDto.getMedicalRecordNumber()));
            patient.addName().setFamily(signupDto.getLastName()).addGiven(signupDto.getFirstName());
            patient.addTelecom().setValue(signupDto.getEmail()).setSystem(ContactPoint.ContactPointSystem.EMAIL);
            patient.setBirthDate(signupDto.getBirthDate());
            patient.setGender(getPatientGender.apply(signupDto.getGenderCode()));
            patient.setActive(true);

            //Add an Identifier
            setIdentifiers(patient, signupDto, signupDto.getMedicalRecordNumber());

            //optional fields
            patient.addAddress().addLine(signupDto.getAddress()).setCity(signupDto.getCity()).setState(signupDto.getStateCode()).setPostalCode(signupDto.getZip());
            if (null != signupDto.getTelephone() && !signupDto.getTelephone().isEmpty())
                patient.addTelecom().setValue(signupDto.getTelephone()).setSystem(ContactPoint.ContactPointSystem.PHONE);
            return patient;
        }
    };

    public Patient convertToPatient(SignupDto signupDto) {
        return signupDtoToPatient.apply(signupDto);
    }

    private void setIdentifiers(Patient patient, SignupDto signupDto, String medicalRecordNumber) {

        patient.addIdentifier().setSystem(identifierProperties.getMrnSystem())
                .setUse(Identifier.IdentifierUse.OFFICIAL).setValue(medicalRecordNumber);

        // setting ssn value
        if (signupDto.getSocialSecurityNumber() != null && signupDto.getSocialSecurityNumber().length() > 0)
            patient.addIdentifier().setSystem(identifierProperties.getSsnSystem())
                    .setValue(signupDto.getSocialSecurityNumber());
    }



}