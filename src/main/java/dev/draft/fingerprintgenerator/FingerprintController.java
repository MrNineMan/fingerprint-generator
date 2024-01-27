package dev.draft.fingerprintgenerator;

import com.fingerprint.model.Confidence;
import com.fingerprint.model.ProductsResponseIdentificationData;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//Beginning of Fingerpint Imports
import com.fingerprint.api.FingerprintApi;
import com.fingerprint.model.EventResponse;
import com.fingerprint.sdk.ApiClient;
import com.fingerprint.sdk.ApiException;
import com.fingerprint.sdk.Configuration;
//End of Fingerprint Imports

import javax.ws.rs.BadRequestException;


@Controller
public class FingerprintController {
   //Replace value with your own Secret API Key
    private static final String FPJS_API_SECRET = "Your API Secret";

    @Autowired
    EntryService entryService;


    @GetMapping("/competitionform")
    public String competitionForm(Model model) {
        model.addAttribute("competitionform", new Entry());
        return "competitionform";
    }
    @PostMapping("/competitionform")
    public String competitionSubmit(@ModelAttribute Entry entry, Model model) {
        model.addAttribute("competitionform", entry);
        try {
            //Validates the legitimacy of the visitorId
            this.fingerprintValidation(entry);
            //Checks to see if an entry with the same visitorId exists in the table
            if (entryService.entryExistByID(entry)) {
                //Attempts to create a new entry
                entryService.createEntry(entry);
                model.addAttribute("title", "Congratulations!");
                model.addAttribute("heading", "Awesome Sauce");
                model.addAttribute("body", "Well done! You have been succesfully entered into our competition!" );
                return "/result";
            } else {
                //If visitorId exists, display fail screen
                model.addAttribute("title", "Ooops Sorry!");
                model.addAttribute("heading", "Oh No!");
                model.addAttribute("body", "It looks like you've already been entered into our competition.");
                return "/result";
            }

        } catch (PSQLException psqlException) {
            //Is triggered if email already exists in the table
            model.addAttribute("title", "Ooops Sorry!");
            model.addAttribute("heading", "Oh No!");
            model.addAttribute("body", "It looks like you've already been entered into our competition using this email address.");
            psqlException.printStackTrace();
            return "/result";
        } catch (BadRequestException badRequestException) {
            //Is triggered if user tries to temper with the VisitorId
            model.addAttribute("title", "Ooops Sorry!");
            model.addAttribute("heading", "Oh No!");
            model.addAttribute("body", "Looks like you've tried to forge the visitor id. Please leave the webiste.");
            badRequestException.printStackTrace();
            return "/result";

        } catch (Exception exception) {
                //Handles all other exceptions
                model.addAttribute("title", "Ooops Sorry!");
                model.addAttribute("heading", "Oh No!");
                model.addAttribute("body", "Looks like we've encountered an error. We're looking at it now");
                exception.printStackTrace();
                return "/result";
        }

    }

    //Performs the bulk of the serverside validations
    private void fingerprintValidation (Entry entry) throws BadRequestException {
        ApiClient client = Configuration.getDefaultApiClient(FPJS_API_SECRET);
        FingerprintApi api = new FingerprintApi(client);
        try {
            // Use the requestId from the client to retrieve an event
            EventResponse response = api.getEvent(entry.getRequestId());
            ProductsResponseIdentificationData productsResponseIdentificationData = response.getProducts().getIdentification().getData();


            //Compares the given visitorId to the visitorId located on the server
            if (productsResponseIdentificationData.getVisitorId() != entry.getVisitorId()) {
                throw new BadRequestException("forged_visitor_id");
            }

            //Compares the indentification timestamp to the request timestamp
            long currentTimeMillis = System.currentTimeMillis();
            long identificationTime = currentTimeMillis / 1000L;
            long diff = currentTimeMillis - identificationTime;
            if (diff > productsResponseIdentificationData.getTimestamp()) {
                throw new BadRequestException("forged_visitor_id_timestamp");
            }

            //Checks the confidence score.
            Confidence confidence = productsResponseIdentificationData.getConfidence();
            /*You can set the minimum confidence score to any value you feel comfortable with.
            * Values must be between 0 and 1.
            */

            if (confidence.getScore() < 0.60) {
                throw new BadRequestException("Not Confident");
            }
        } catch (ApiException apiException) {
            apiException.printStackTrace();
        }
    }

}
