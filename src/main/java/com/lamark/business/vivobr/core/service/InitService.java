package com.lamark.business.vivobr.core.service;
 
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class InitService {
   private static String propsFileName = "SMS_BILLING_HTTP_VIVO_BR.properties";
   private String DB_CONF_IS_ON_TOP_OF_SUPPORTED = "IS_ON_TOP_OF_SUPPORTED";
   private String DB_CONF_MANAGEMENT_SERVICE_TIMEOUT_IN_SEC = "MANAGEMENT_SERVICE_TIMEOUT_IN_SEC";
   public static final String spId = "001851";
   public static final String password = "Lamark123";
   public static final int serviceType = 2;
   public static final int TIMES = 1;
   public int smsTPS = 100;
   public int billingTPS = 100;
   private String siteName = "";
   private String[] sites;
   private Map<Integer, Map<String, String>> serviceURLMap = new HashMap();
   private Map<String, Map<String, String>> sitePropsMap = new HashMap();
   private Map<String, Map<String, String>> siteConfigsMap = new HashMap();
   private Map<Integer, Map<String, String>> serviceIdPropsMap = new HashMap();
   private Map<String, String> operatorsMap = new HashMap();
   private static HashMap<String, String> serviceIDs = new HashMap();
   private List<String> prefixes = new ArrayList();
   public String smsCountryCode = "";
   public int smsMaxLength = 10;
   private Properties properties;
   private HashMap<String, Integer> sitesMap = new HashMap();
   private boolean returnWith9;
   private static String blockedSmsMsg;
   private static long SEQUENCE_NUMBER = Calendar.getInstance().getTimeInMillis();
   private static final String LA_SPLITTER = "_";
   private static InitService instance;

   /////////////////////////////////////////////////////////NUEVAS CONFIGURACIONES PROYECTO QUARKUS/////////////////////////////////////////////////////////


   @ConfigProperty(name = "sites")
   public String sitesProp;

   @ConfigProperty(name = "channels")
   public String channelsProp;


   private InitService() {
      this.initParameters();
   }

   private void initParameters() {
      try {
         //CAMBIADO POR NUEVA ARQUITECTURA

        /*
         String sitesProp = this.properties.getProperty("sites");
         String channelsProp = this.properties.getProperty("channels");
         String numberOfBillingTPS = this.properties.getProperty("billingTPS");
         String numberOfSMSTPS = this.properties.getProperty("smsTPS");
         blockedSmsMsg = this.properties.getProperty("blockedSmsMsg");
         */

         this.sites = sitesProp.split(",");
         String[] channels = channelsProp.split(",");
         if (this.sites != null && this.sites.length > 0) {
            this.siteName = this.sites[0];
         }

         String[] var12;
         int var11 = (var12 = this.sites).length;

         for(int var10 = 0; var10 < var11; ++var10) {
            String siteName = var12[var10];
            this.initSite(this.properties, channels, siteName);
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   private void initSite(Properties properties, String[] channels, String siteName) {
      Map<String, String> channelsMap = new HashMap();
      Map<String, String> configsMap = new HashMap();
      String numbersProps = properties.getProperty(siteName);
      String[] numbers = numbersProps.split(",");
      Map<String, String> serviceIdPerSiteMap = new HashMap();
      String[] serviceIdPerSite = properties.getProperty("ServiceId " + siteName).split(",");

      for(int i = 0; i < channels.length; ++i) {
         channelsMap.put(channels[i], numbers[i]);
         serviceIdPerSiteMap.put(numbers[i], serviceIdPerSite[i]);
      }

      List<Object[]> siteParameters = this.getSiteConfiguration(siteName);
      Iterator var12 = siteParameters.iterator();

      while(var12.hasNext()) {
         Object[] siteParameter = (Object[])var12.next();
         String configurationName = (String)siteParameter[0];
         String configurationValue = (String)siteParameter[1];
         configsMap.put(configurationName, configurationValue);
      }

//      this.sitesMap.put(siteName, this.getSiteID(siteName));
      this.sitePropsMap.put(siteName, channelsMap);
//      this.serviceURLMap.put(this.getSiteID(siteName), this.getServicesURL(siteName));
      this.siteConfigsMap.put(siteName, configsMap);
//      this.serviceIdPropsMap.put(this.getSiteID(siteName), serviceIdPerSiteMap);
      this.prefixes.addAll(this.loadPrefixes(siteName));
   }

   protected List<Object[]> getSiteConfiguration(String site) {
      List<Object[]> siteParams = new ArrayList();
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection conn = null;

      try {
         conn = this.getConnection();
         String query = "SELECT * FROM site_configuration WHERE siteID = (SELECT siteID FROM site WHERE siteName='" + site + "') " + "AND (configurationName LIKE 'SMS%' OR configurationName LIKE 'IS_ON_TOP%' OR configurationName LIKE 'MANAGEMENT%');";
         System.out.println(query);
         ps = conn.prepareStatement(query);
         rs = ps.executeQuery();

         while(rs.next()) {
            Object[] o = new Object[]{rs.getString("configurationName"), rs.getString("configurationValue")};
            siteParams.add(o);
         }
      } catch (Throwable var16) {
         var16.printStackTrace();
      } finally {
         try {
            this.disconnect(ps, rs, conn);
         } catch (Exception var15) {
            var15.printStackTrace();
         }

      }

      return siteParams;
   }

   private List<String> loadPrefixes(String site) {
      List<String> prefixes = new ArrayList();
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection conn = null;

      try {
         conn = this.getConnection();
         String query = "SELECT prefix_msisdn FROM product p join products_of_site ps on p.productID=ps.productID WHERE ps.siteID IN (SELECT siteID FROM site WHERE siteName LIKE '" + site + "%');";
         ps = conn.prepareStatement(query);
         rs = ps.executeQuery();

         while(rs.next()) {
            prefixes.add(rs.getString(1));
         }
      } catch (Throwable var15) {
         var15.printStackTrace();
      } finally {
         try {
            this.disconnect(ps, rs, conn);
         } catch (Exception var14) {
            var14.printStackTrace();
         }

      }

      return prefixes;
   }

   public String getChannelForSite(String number, String siteName) {
      Map<String, String> map = (Map)this.getSitePropsMap().get(siteName);
      Iterator iter = map.keySet().iterator();

      while(iter.hasNext()) {
         String ch = (String)iter.next();
         if (((String)map.get(ch)).equals(number)) {
            return ch;
         }
      }

      return null;
   }

   public String getSiteFromNumber(String number) {
      String siteName = "";
      Map<String, Map<String, String>> props = this.getSitePropsMap();
      Iterator i = props.keySet().iterator();

      while(i.hasNext()) {
         siteName = (String)i.next();
         Map<String, String> map = (Map)props.get(siteName);
         Iterator iter = map.keySet().iterator();

         while(iter.hasNext()) {
            String ch = (String)iter.next();
            if (((String)map.get(ch)).equals(number)) {
               return siteName;
            }
         }
      }

      return siteName;
   }

   public String getJmsHost(String siteName) {
      Map<String, String> map = (Map)this.getSiteConfigsMap().get(siteName);
      return (String)map.get("SMSInternalIP");
   }

   public String encodePassword(String password, String algorithm) {
      byte[] unencodedPassword = password.getBytes();
      MessageDigest md = null;

      try {
         md = MessageDigest.getInstance(algorithm);
      } catch (Exception var8) {
         var8.printStackTrace();
         return password;
      }

      md.reset();
      md.update(unencodedPassword);
      byte[] encodedPassword = md.digest();
      StringBuffer buf = new StringBuffer();

      for(int i = 0; i < encodedPassword.length; ++i) {
         if ((encodedPassword[i] & 255) < 16) {
            buf.append("0");
         }

         buf.append(Long.toString((long)(encodedPassword[i] & 255), 16));
      }

      return buf.toString();
   }

   public Map<Integer, Map<String, String>> getServiceIdPropsMap() {
      return this.serviceIdPropsMap;
   }

   public Map<String, Map<String, String>> getSitePropsMap() {
      return this.sitePropsMap;
   }

   public Map<String, Map<String, String>> getSiteConfigsMap() {
      return this.siteConfigsMap;
   }

   public Map<String, String> getOperatorsMap() {
      return this.operatorsMap;
   }

   public List<String> getPrefixes() {
      return this.prefixes;
   }

   public String getNumberForChannel(String channel, String siteName) {
      String result = "";
      Map<String, String> map = (Map)this.getSitePropsMap().get(siteName);
      result = (String)map.get(channel);
      if ("-1".equals(result)) {
         Iterator var6 = map.entrySet().iterator();

         while(var6.hasNext()) {
            Entry<String, String> entry = (Entry)var6.next();
            if (!"-1".equals(entry.getValue())) {
               result = (String)entry.getValue();
               break;
            }
         }
      }

      return result;
   }

   public String getOperatorNum(String s) {
      int num = Integer.parseInt(s);
      String result = "mainChannel";
      switch(num) {
      case 2:
         result = "quizChannel";
         break;
      case 3:
         result = "challengeChannel";
      }

      return result;
   }

//   protected Map<String, String> getServicesURL(String site) {
//      List<Object[]> confParams = null;
//      HashMap confURLs = null;
//
//      try {
//         SessionFactory sessionFactory = (new Configuration()).configure().buildSessionFactory();
//         Session session = sessionFactory.openSession();
//         session.beginTransaction();
//         String query = "SELECT configurationName, configurationValue FROM site_configuration WHERE siteID=(SELECT siteID FROM site WHERE siteName='" + site + "') " + " AND ( configurationName = 'SendSmsService' OR " + "configurationName = 'SMAPService' OR configurationName = 'SmsNotificationManagerService' );";
//         SQLQuery q = session.createSQLQuery(query);
//         confParams = q.list();
//         if (confParams != null && confParams.size() > 0) {
//            confURLs = new HashMap();
//            confURLs.put("SendSmsService", (String)((Object[])confParams.get(0))[1]);
//            confURLs.put("SMAPService", (String)((Object[])confParams.get(1))[1]);
//            confURLs.put("SmsNotificationManagerService", (String)((Object[])confParams.get(2))[1]);
//         }
//
//         session.close();
//      } catch (Throwable var8) {
//         throw new ExceptionInInitializerError(var8);
//      }
//
//      return confURLs;
//   }

   private Connection getConnection() throws NamingException, SQLException {
      Context ctx = new InitialContext();
      DataSource ds = (DataSource)ctx.lookup("java:/MySqlDS");
      return ds.getConnection();
   }

   private void disconnect(PreparedStatement ps, ResultSet rs, Connection conn) throws SQLException {
      if (rs != null) {
         rs.close();
      }

      if (ps != null) {
         ps.close();
      }

      if (conn != null) {
         conn.close();
      }

   }

//   public synchronized String getServiceId(int siteID, String priceCodeID) {
//      String serviceID = (String)serviceIDs.get(priceCodeID);
//      if (serviceID != null) {
//         return serviceID;
//      } else {
//         try {
//            Context ctx = new InitialContext();
//            SiteManagementServices siteManagement = (SiteManagementServices)ctx.lookup("SiteManagementServicesBean/local");
//            serviceID = siteManagement.getPriceCodeByCode(priceCodeID, siteID).getServiceID();
//         } catch (NamingException var6) {
//            var6.printStackTrace();
//         }
//
//         serviceIDs.put(priceCodeID, serviceID);
//         return serviceID;
//      }
//   }

//   public String getEndpoint(int siteID) {
//      String endPoint = null;
//
//      try {
//         Context ctx = new InitialContext();
//         SiteManagementServices siteManagement = (SiteManagementServices)ctx.lookup("SiteManagementServicesBean/local");
//         endPoint = siteManagement.getSiteConfig(new SiteConfigurationId("SMS_ENDPOINT", siteID)).getConfigurationValue();
//      } catch (NamingException var5) {
//         var5.printStackTrace();
//      }
//
//      return endPoint;
//   }

   public String getSiteName() {
      return this.siteName;
   }

   public int getSmsTPS() {
      return this.smsTPS;
   }

   public int getBillingTPS() {
      return this.billingTPS;
   }

   public Map<Integer, Map<String, String>> getServiceURLMap() {
      return this.serviceURLMap;
   }

   public String getSmsCountryCode() {
      return this.smsCountryCode;
   }

   public int getSmsMaxLength() {
      return this.smsMaxLength;
   }

   public Properties getProperties() {
      return this.properties;
   }

   public Integer getSiteID() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

//   public Integer getSiteID(String siteName) {
//      int siteID = -1;
//      if (this.sitesMap != null && this.sitesMap.get(siteName) != null) {
//         return (Integer)this.sitesMap.get(siteName);
//      } else {
//         try {
//            Context ctx = new InitialContext();
//            SiteManagementServices siteManagement = (SiteManagementServices)ctx.lookup("SiteManagementServicesBean/local");
//            siteID = siteManagement.getSiteByName(siteName).getSiteId();
//         } catch (Throwable var5) {
//            var5.printStackTrace();
//         }
//
//         return siteID;
//      }
//   }

   public static synchronized int getSequenceNumber() throws NumberFormatException {
      long sequenceNumber = ++SEQUENCE_NUMBER;
      String sequenceNumberStr = String.valueOf(sequenceNumber);
      sequenceNumberStr = sequenceNumberStr.substring(sequenceNumberStr.length() - 9, sequenceNumberStr.length());
      int transactionId = Math.abs(Integer.parseInt("2" + sequenceNumberStr.substring(1, sequenceNumberStr.length())));
      return transactionId;
   }

   public List<String> invokePriceCodesBillingSMS(String siteName) {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public Hashtable<String, List<String>> getPriceCodesBillingSMS() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public Hashtable<String, List<String>> getPriceCodesForFree() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public String getSiteNameFromID(int siteID) {
      StringBuilder sb = new StringBuilder();
      Iterator var4 = this.sitesMap.entrySet().iterator();

      Entry siteEntry;
      while(var4.hasNext()) {
         siteEntry = (Entry)var4.next();
         sb.append((String)siteEntry.getKey() + "-" + siteEntry.getValue() + "; ");
      }

      if (this.sitesMap.containsValue(siteID)) {
         var4 = this.sitesMap.entrySet().iterator();

         while(var4.hasNext()) {
            siteEntry = (Entry)var4.next();
            if (((Integer)siteEntry.getValue()).equals(siteID)) {
               return (String)siteEntry.getKey();
            }
         }
      }

      return "";
   }

   public Map<String, String> getAccessCode() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public String getSiteConfigurationElement(String siteName, String propertyName) {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public String getPropsFileName() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public void addToAudit(String msisdn, String message) {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public boolean isAuthenticationNeeded() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   

   public String getCpID() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public String getServiceID() {
      throw new UnsupportedOperationException("Not a valid operation!");
   }

   public boolean isReturnWith9() {
      return this.returnWith9;
   }

   public String getBlockedSmsMsg() {
      return blockedSmsMsg;
   }

   public static String cutShortCode(String shortCode) {
      String[] split = StringUtils.isNotBlank(shortCode) ? shortCode.split("_") : new String[0];
      if (split.length > 0) {
         shortCode = split[0];
      }

      return shortCode;
   }

   public Map<String, String> getDBConfigurationMap(String siteName) {
      Object dbConfigurationForSite = new HashMap();

      try {
         if (this.siteConfigsMap.containsKey(siteName)) {
            Map<String, String> tmpConfig = (Map)this.siteConfigsMap.get(siteName);
            if (tmpConfig != null) {
               dbConfigurationForSite = tmpConfig;
            }
         }
      } catch (Exception var4) {
      }

      return (Map)dbConfigurationForSite;
   }

   public String getDBConfigurationProperty(String siteName, String propertyName) {
      Map<String, String> dbConf = this.getDBConfigurationMap(siteName);
      String propertyValue = (String)dbConf.get(propertyName);
      return propertyValue;
   }

   public boolean isOnTopOfSupported(String siteName) {
      boolean isSupprted = false;
      String onTopOffDbProperty = this.getDBConfigurationProperty(siteName, this.DB_CONF_IS_ON_TOP_OF_SUPPORTED);
      if (onTopOffDbProperty != null) {
         try {
            isSupprted = Boolean.valueOf(onTopOffDbProperty);
         } catch (Exception var5) {
         }
      }

      return isSupprted;
   }

   public int getManagementServiceTimoutInMilliSec(String siteName) {
      String managementServiceTimeoutDbProperty = this.getDBConfigurationProperty(siteName, this.DB_CONF_MANAGEMENT_SERVICE_TIMEOUT_IN_SEC);
      int managementServiceTimeoutInMilliSec = 5000;
      if (managementServiceTimeoutDbProperty != null) {
         try {
            managementServiceTimeoutInMilliSec = Integer.parseInt(managementServiceTimeoutDbProperty) * 1000;
         } catch (Exception var5) {
         }
      }

      return managementServiceTimeoutInMilliSec;
   }
}
