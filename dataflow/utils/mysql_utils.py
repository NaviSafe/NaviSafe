def process_batch(batch_df, batch_id):
    print(f"--- 배치 {batch_id} ---", flush=True)
    batch_df.show(5, truncate=False)
    #display(batch_df.limit(10))  # 주피터 셀에서 상위 10개 확인

    batch_df.write \
      .format("jdbc") \
      .option("url", "jdbc:mysql://mysql:3306/toy_project") \
      .option("driver", "com.mysql.cj.jdbc.Driver") \
      .option("dbtable", 테이블명) \
      .option("user", "user") \
      .option("password", "userpass") \
      .mode("append") \
      .save()