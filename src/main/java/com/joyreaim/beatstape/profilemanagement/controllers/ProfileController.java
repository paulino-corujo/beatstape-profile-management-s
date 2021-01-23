package com.joyreaim.beatstape.profilemanagement.controllers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.joyreaim.beatstape.profilemanagement.db.DBClient;
import com.joyreaim.beatstape.profilemanagement.dto.ProfileInfo;
import com.joyreaim.beatstape.profilemanagement.s3.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.time.Instant;

@RequestMapping(value = "/api")
@RestController
@Slf4j
public class ProfileController {

    private static final Integer MAX_UPLOAD = 1024 * 1024;
    private static final String JPEG_CONTENT_TYPE = "image/jpeg";

    @Autowired
    DBClient dbClient;

    @Autowired
    FileService fileService;

    @GetMapping(value = "/unprotected-data")
    public String getName() {
        return "Hello, this api is not protected.";
    }

    @GetMapping(value = "/profile")
    @RolesAllowed("user")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        Long now = Instant.now().toEpochMilli();
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("id", principal.getName())
                .withUpdateExpression("set #ct = if_not_exists(#ct , :val1),  #lut= if_not_exists(#lut, :val2)")
                .withNameMap(new NameMap().with("#ct", "createdTimestamp").with("#lut", "lastUpdateTimestamp"))
                .withValueMap(
                        new ValueMap().withLong(":val1", now).withLong(":val2", now))
                .withReturnValues(ReturnValue.ALL_NEW);
        try {
            DynamoDB dynamoDB = new DynamoDB(dbClient.getClient());
            UpdateItemOutcome beatstapeProfile = dynamoDB.getTable("beatstape_profile").updateItem(updateItemSpec);
            System.out.println(beatstapeProfile);
            ProfileInfo profile = new ProfileInfo();
            BeanUtils.populate(profile, beatstapeProfile.getItem().asMap());
            System.out.println(profile);
            if (now.equals(profile.getCreatedTimestamp()) && now.equals(profile.getLastUpdateTimestamp())) {
                return ResponseEntity.status(HttpStatus.CREATED).body(profile);
            }
            return ResponseEntity.status(HttpStatus.OK).body(profile);
        } catch (Exception e) {
            log.error("Error trying to retrieve user profile {}", principal.getName(), e);
            return new ResponseEntity<Error>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PutMapping(value = "/profile")
    @RolesAllowed("user")
    public ResponseEntity<?> updateUserProfile(Principal principal, @RequestBody ProfileInfo profileInfo) {
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("id", principal.getName())
                .withUpdateExpression("set #bio= :val1, #un = if_not_exists(#un , :val2), #an = :val3, #lut = :val4")
                .withNameMap(new NameMap().with("#bio", "bio").with("#un", "userName").with("#an", "artistName").
                        with("#lut", "lastUpdateTimestamp"))
                .withValueMap(
                        new ValueMap().withString(":val1", profileInfo.getBio())
                                .withString(":val2", profileInfo.getUserName())
                                .withString(":val3", profileInfo.getArtistName())
                                .withLong(":val4", Instant.now().toEpochMilli()))
                .withReturnValues(ReturnValue.ALL_NEW);

        try {
            DynamoDB dynamoDB = new DynamoDB(dbClient.getClient());
            UpdateItemOutcome beatstapeProfile = dynamoDB.getTable("beatstape_profile").updateItem(updateItemSpec);
            ProfileInfo profile = new ProfileInfo();
            BeanUtils.populate(profile, beatstapeProfile.getItem().asMap());
            return ResponseEntity.status(HttpStatus.OK).body(profile);
        } catch (Exception e) {
            log.error("Error trying to retrieve user profile {}", principal.getName(), e);
            return new ResponseEntity<Error>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PostMapping(value = "/profile/avatar", consumes = "multipart/form-data")
    @RolesAllowed("user")
    public ResponseEntity<?> updateProfileAvatar(Principal principal, @RequestBody MultipartFile profileAvatar) {
        try {
            if (profileAvatar.getSize() > MAX_UPLOAD || !JPEG_CONTENT_TYPE.equals(profileAvatar.getContentType())) {
                return new ResponseEntity<Error>(HttpStatus.BAD_REQUEST);
            }
            try (InputStream bufferedIn = new BufferedInputStream(profileAvatar.getInputStream())) {
                byte[] fileByteArray = IOUtils.toByteArray(bufferedIn);
                fileService.writeResource(principal.getName(), fileByteArray);
                return ResponseEntity.status(HttpStatus.CREATED).body(null);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error reading avatar file");
            }
        } catch (Exception e) {
            log.error("Error trying to retrieve user profile {}", principal.getName(), e);
            return new ResponseEntity<Error>(HttpStatus.BAD_REQUEST);
        }
    }
}
