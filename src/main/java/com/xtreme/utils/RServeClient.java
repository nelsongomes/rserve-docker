package com.xtreme.utils;

import org.apache.commons.io.FileUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RServeClient {
  private static String R_REPO_URL = "http://cran.rstudio.com/";

  public static void main(final String[] args) {
    try {
      final RConnection conn = new RConnection("127.0.0.1", 6311);

      // R version
      final REXP x = conn.eval("R.version.string");
      System.out.println(x.asString());

      // preload libraries
      RServeClient.loadLibrary("forecast", conn);

      final double[] f = RServeClient.autoArimaForecast("/Users/ngomes/Documents/workspace/forecast/sales2.csv", 20, conn);

    } catch (final RserveException e) {
      System.out.println(e);
      e.printStackTrace();
    } catch (final REXPMismatchException e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  /**
   * Method to forecast using data from data file.
   * 
   * @param datafile
   * @param values
   * @param conn
   * @return
   */
  public static double[] autoArimaForecast(final String datafile, final int values,
      final RConnection conn) {
    String filename = null;
    try {
      // load dependency
      RServeClient.loadLibrary("forecast", conn);

      // create remote file
      filename = RServeClient.createRemoteFile(datafile, conn);

      if (filename != null) {
        // System.out.println(filename);

        // execute forecast (with zero header lines skip=0)
        conn.eval("fit <- auto.arima(scan(file='" + filename + "', skip = 0))");

        // forecast next h events
        conn.eval("result <- forecast(fit,h=" + values + ")");

        // fetch values (mean)
        final double[] forecasted = conn.eval("result$mean").asDoubles();

        return forecasted;
      }
    } catch (final RserveException e) {
      e.printStackTrace();
    } catch (final REXPMismatchException e) {
      e.printStackTrace();
    } finally {
      // remove remote file
      if (filename != null) {
        try {
          conn.removeFile(filename);
        } catch (final RserveException e) {
          // do nothing
          e.printStackTrace();
        }
      }
    }

    return null;
  }

  /**
   * Method to install R package and load it.
   *
   * @param library
   *          library name
   * @param repo
   *          repository link
   * @param conn
   *          Connection object
   * @throws RserveException
   *           error
   * @throws REXPMismatchException
   *           error
   */
  public static void loadLibrary(final String library, final RConnection conn)
      throws RserveException, REXPMismatchException {
    // import lib
    REXP x = conn.eval("require('" + library + "')");
    if (x.asInteger() == 0) {
      x = conn.eval("install.packages('" + library + "', repos='" + RServeClient.R_REPO_URL + "')");
      x = conn.eval("library('" + library + "')");
    }
  }

  /**
   * Method to create remote data file.
   *
   * @param file
   *          filename to read.
   * @param conn
   *          Connection to Rserve
   * @return new remote filename
   */
  public static String createRemoteFile(final String file, final RConnection conn) {
    final String filename = UUID.randomUUID().toString() + ".data";

    try (final RFileOutputStream stream = conn.createFile(filename);) {
      stream.write(FileUtils.readFileToByteArray(new File(file)));
      stream.flush();
      stream.close();
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }

    return filename;
  }
}