package org.easystogu.trendmode;

import net.sf.json.JSONObject;
import org.easystogu.file.TextFileSourceHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.trendmode.vo.SimplePriceVO;
import org.easystogu.trendmode.vo.TrendModeVO;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

//@Component
public class TrendModeLoader {
    private static Logger logger = LogHelper.getLogger(TrendModeLoader.class);
    private TextFileSourceHelper fileSource = TextFileSourceHelper.getInstance();
    private Map<String, TrendModeVO> trendModeMap = new HashMap<String, TrendModeVO>();
    private static Map<String, Class> classMap = new HashMap<String, Class>();
    //just for legacy using, instead of using @Component
    private static TrendModeLoader instance = null;

    static {
        classMap.put("prices", SimplePriceVO.class);
    }

    private TrendModeLoader() {
        startUp();
    }

    public static TrendModeLoader getInstance(){
        if(instance == null){
            instance = new TrendModeLoader();
        }
        return instance;
    }

    @PostConstruct
    public void startUp() {
        try {
            List<String> names = fileSource.listResourceFiles("classpath:/TrendMode/*.json");
            for (String name : names) {
                JSONObject jsonObject = JSONObject.fromObject(fileSource.loadContent("TrendMode/" + name));
                TrendModeVO tmo = (TrendModeVO) JSONObject.toBean(jsonObject, TrendModeVO.class, classMap);
                trendModeMap.put(name.split("\\.")[0], tmo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutDown() {

    }

    public TrendModeVO loadTrendMode(String name) {
        TrendModeVO tmo = this.trendModeMap.get(name);
        if (tmo == null) {
            logger.error("loadTrendMode is null for " + name);
        }
        return tmo;
    }

    public List<String> getAllNames() {
        List<String> names = new ArrayList<String>();
        String[] str = this.trendModeMap.keySet().toArray(new String[0]);
        for (String s : str) {
            names.add(s);
        }
        return names;
    }
    
    //display error when have Chinese
    public List<String> getAllDisplayNames() {
        List<String> names = new ArrayList<String>();
        Collection<TrendModeVO> list = this.trendModeMap.values();
        for (TrendModeVO tmo : list) {
            names.add(tmo.name);
        }
        return names;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        TrendModeLoader ins = new TrendModeLoader();
        ins.startUp();
        ins.loadTrendMode("test");
    }

}
