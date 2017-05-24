package in.gvc;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

@Controller
public class WebController {


    Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/")
    public String blank_index() {
        return "upload";
    }

    @RequestMapping(value = {"/upload"}, method = RequestMethod.POST)
    @ResponseBody
    public String uploadFileHandler(@RequestParam("file") MultipartFile file) {

        if (!file.isEmpty())
        {
            try
            {
                byte[] bytes = file.getBytes();

                // Creating the directory to store file
                String rootPath = "webcam";
                File dir = new File(rootPath);
                if (!dir.exists())
                    dir.mkdirs();

                // Create the file on server
                File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();

                logger.info("Server File Location=" + serverFile.getAbsolutePath());

                MyBean bean = new MyBean(serverFile);
                mongoTemplate.insert(bean);

                return "You successfully uploaded file=" + file.getOriginalFilename();
            }
            catch (Exception e)
            {
                return "You failed to upload " + file.getOriginalFilename() + " => " + e.getMessage();
            }
        }
        else
        {
            return "You failed to upload " + file.getOriginalFilename() + " because the file was empty.";
        }
    }
    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping(value={"/search"},method = RequestMethod.GET)
    @ResponseBody
    public List<MyBean> search(@RequestParam Map<String,String> allRequestParams, ModelMap model) {

        Query searchUserQuery = new Query();

        for(Map.Entry<String,String> entry : allRequestParams.entrySet())
        {
            if(entry.getValue()==null)
                entry.setValue("");
            searchUserQuery.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
        }

        List<MyBean> list = mongoTemplate.find(searchUserQuery,MyBean.class);
        for(MyBean bean : list)
        {
            bean.file = "http://159.203.112.66:80/viewAttach?file="+bean.file;
        }

        return list;
    }

    @RequestMapping(value="/viewAttach", method = RequestMethod.GET)
    public void doDownload(@RequestParam("file") String filename,HttpServletRequest request, HttpServletResponse response) throws IOException {

        // get absolute path of the application
        ServletContext context = request.getServletContext();
        String appPath = context.getRealPath("");
        //System.out.println("appPath = " + appPath);

        // construct the complete absolute path of the file
        String fullPath = "webcam/" + filename;
        File downloadFile = new File(fullPath);
        FileInputStream inputStream = new FileInputStream(downloadFile);

        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        //System.out.println("MIME type: " + mimeType);

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();

    }

    @RequestMapping(value="/delete", method = RequestMethod.GET)
    @ResponseBody
    public List<MyBean> delete(@RequestParam Map<String,String> allRequestParams) throws IOException {
        Query searchUserQuery = new Query();

        for(Map.Entry<String,String> entry : allRequestParams.entrySet())
        {
            if(entry.getValue()==null)
                entry.setValue("");
            searchUserQuery.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
        }

        List<MyBean> list = mongoTemplate.find(searchUserQuery,MyBean.class);
        for(MyBean bean : list)
        {
            File file = new File(bean.file);
            file.delete();
            logger.info("File deleted: "+file.getAbsoluteFile());

        }
        mongoTemplate.remove(searchUserQuery,MyBean.class);
        return list;
    }


    }