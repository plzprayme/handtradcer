package com.odengmin.handtracer.global.view;

import android.app.Application;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.odengmin.handtracer.database.local.dao.GpsDao;
import com.odengmin.handtracer.database.local.GpsDataBase;
import com.odengmin.handtracer.database.local.entity.GpsEntity;
import com.odengmin.handtracer.database.local.entity.WashCount;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GpsViewModel extends AndroidViewModel {
    private GpsDataBase gpsDataBase;
    public GpsViewModel(@NonNull Application application) {
        super(application);
        /* init database */
        if(gpsDataBase == null) {
            gpsDataBase = GpsDataBase.getInstance(application);
        }
    }
    public LiveData<List<GpsEntity>> getAllGps() {
        return gpsDataBase.GpsDao().getAllGps();
    }

    public GpsEntity getLastGps() throws ExecutionException, InterruptedException {
        return new GetLastAsyncTask(gpsDataBase.GpsDao()).execute().get();
    }

    public List<GpsEntity> getDateGps(String date) throws ExecutionException, InterruptedException {
        return new GetAsyncTask(gpsDataBase.GpsDao()).execute(date).get();
    }

    public void insert(GpsEntity gpsEntity) {
        new InsertAsyncTask(gpsDataBase.GpsDao())
                .execute(gpsEntity);
    }

    private static class InsertAsyncTask extends AsyncTask<GpsEntity, Void, Void> {
        private GpsDao gpsDao;

        public InsertAsyncTask(GpsDao gpsDao) {
            this.gpsDao = gpsDao;
        }

        @Override
        protected Void doInBackground(GpsEntity... gpsEntities) {
            Log.i("gpsEntities", gpsEntities[0].toString());
            gpsDao.insertUsers(gpsEntities[0]);
            return null;
        }
    }

    public static class GetAsyncTask extends AsyncTask<String, Void, List<GpsEntity>> {
        private GpsDao gpsDao;

        public GetAsyncTask(GpsDao gpsDao) {
            this.gpsDao = gpsDao;
        }

        @Override
        public List<GpsEntity> doInBackground(String... strings) {
            return gpsDao.getDateGps(strings[0]);
            //return gpsDao.getDateGps();
        }
    }

    public static class GetLastAsyncTask extends AsyncTask<Void, Void, GpsEntity> {
        private GpsDao gpsDao;

        public GetLastAsyncTask(GpsDao gpsDao) {
            this.gpsDao = gpsDao;
        }

        @Override
        public GpsEntity doInBackground(Void... voids) {
            return gpsDao.getLastGps();
            //return gpsDao.getDateGps();
        }
    }

    public WashCount startCountLogic(String date) throws ExecutionException, InterruptedException {
        return new startCountLogicTask(gpsDataBase.GpsDao()).execute(date).get();
    }

    private static class startCountLogicTask extends AsyncTask<String, Void, WashCount> {
        private GpsDao gpsDao;

        public startCountLogicTask(GpsDao gpsDao) { this.gpsDao=gpsDao;}

        @Override
        protected WashCount doInBackground(String... dates) {
            WashCount entity = gpsDao.getTodayWashCount(dates[0]);
            if (entity == null) {
                gpsDao.insertWashCount(new WashCount());
                entity = gpsDao.getTodayWashCount(dates[0]);
            }

            entity.setWashCount(entity.getWashCount() + 1);
            gpsDao.updateWashCount(entity);

            return entity;
        }
    }
}
