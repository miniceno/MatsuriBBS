package com.example.matsuribbsandroid.post;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matsuribbsandroid.R;
import com.example.matsuribbsandroid.XCRoundImageView;
import com.example.matsuribbsandroid.entity.Post;
import com.example.matsuribbsandroid.entity.Reply;
import com.example.matsuribbsandroid.service.MatsuriBBSManager;
import com.example.matsuribbsandroid.service.MatsuriBBSService;
import com.makeramen.roundedimageview.RoundedImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.security.AccessController.getContext;

public class PostActivity extends AppCompatActivity {

    @BindView(R.id.btn_collection)
    ImageView btnCollection;
    @BindView(R.id.postdetail_user_avatar)
    RoundedImageView postdetailUserAvatar;
    @BindView(R.id.postdetail_username)
    TextView postdetailUsername;
    @BindView(R.id.postdetail_date)
    TextView postdetailDate;
    @BindView(R.id.postdetail_btn_follow)
    Button postdetailBtnFollow;
    @BindView(R.id.postdetail_title)
    TextView postdetailTitle;
    @BindView(R.id.postdetail_content)
    TextView postdetailContent;
    @BindView(R.id.postdetail_replyNum)
    TextView postdetailReplyNum;
    @BindView(R.id.postdetail_likeNum)
    TextView postdetailLikeNum;
    @BindView(R.id.postdetail_toolbar)
    Toolbar postdetailToolbar;
    @BindView(R.id.postdetail_refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.postdetail_reply_recycleview)
    RecyclerView recyclerView;
    private Integer pid;
    private Post post;
    private PostAdapter postAdapter;
    private List<Reply> replyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        ImageView btncollection = findViewById(R.id.btn_collection);

        //开启toolbar返回按钮
        setSupportActionBar(postdetailToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        Intent intent = getIntent();
        pid = intent.getIntExtra("pid", -1);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        postAdapter = new postAdapter(replyList,getContext(),R.layout.activity_post_replyItem);
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPostDetail();
    }

    private void loadPostDetail() {
        MatsuriBBSService service = MatsuriBBSManager.createOpenApiService();
        service.viewPostDetail(pid).enqueue(new Callback<PostDetailResponse>() {
            @Override
            public void onResponse(Call<PostDetailResponse> call, Response<PostDetailResponse> response) {
                if (!response.body().isError() && response.body().getCode() == 200 && response.body().getData() != null) {
                    post = response.body().getData();
                    setPostDetail(post);
                } else {
                    Toast.makeText(PostActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostDetailResponse> call, Throwable t) {
                Toast.makeText(PostActivity.this, "网络访问失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setPostDetail(Post postDetail) {
        postdetailTitle.setText(postDetail.getTitle());
        postdetailContent.setText(postDetail.getContent());
        postdetailUsername.setText(postDetail.getAuthor().getUserName());
        Picasso.get()
                .load(postDetail.getAuthor().getAvatar())
                .placeholder(R.drawable.avatar)
                .into(postdetailUserAvatar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        postdetailDate.setText(dateFormat.format(postDetail.getPostDate()));
        postdetailReplyNum.setText(postDetail.getReplyNum().toString());
        postdetailLikeNum.setText(postDetail.getLikeNum().toString());
    }

    static class PostReplyViewHolder extends RecyclerView.ViewHolder{
        XCRoundImageView replyItem_user_avatar;
        TextView replyItem_userName;
        TextView replyItem_replyDate;
        TextView replyItem_likeNum;
        TextView replyItem_content;
        TextView replyItem_viewMore;

        public PostReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.replyItem_user_avatar = itemView.findViewById(R.id.replyItem_user_avatar);
            this.replyItem_userName = itemView.findViewById(R.id.replyItem_userName);
            this.replyItem_replyDate = itemView.findViewById(R.id.replyItem_replyDate);
            this.replyItem_likeNum = itemView.findViewById(R.id.replyItem_likeNum);
            this.replyItem_content = itemView.findViewById(R.id.replyItem_content);
            this.replyItem_viewMore = itemView.findViewById(R.id.replyItem_viewMore);
        }

        public void updatePostReply(Reply reply) {
            Context context = itemView.getContext();
            if (reply == null || context == null){
                return;
            }
            Picasso.get()
                    .load(reply.getAuthor().getAvatar())
                    .placeholder(R.drawable.avatar)
                    .into(replyItem_user_avatar);
            replyItem_userName.setText(reply.getAuthor().getUserName());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            replyItem_replyDate.setText(dateFormat.format(reply.getReplyDate()));
            replyItem_likeNum.setText(reply.getLikeNum().toString());
            replyItem_content.setText(reply.getContent());
            replyItem_viewMore.setText("查看"+reply.getSubReplyNum()+"条回复");
        }
    }

    class postAdapter extends RecyclerView.Adapter<PostReplyViewHolder> {
        private List<Reply> reply;
        private Context context;

        @LayoutRes
        private int layoutResId;

        public postAdapter(List<Reply> reply, Context context, int layoutResId) {
            this.reply = reply;
            this.context = context;
            this.layoutResId = layoutResId;
        }

        @NonNull
        @Override
        public PostReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
            return new PostReplyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PostReplyViewHolder holder, int position) {
            if (holder == null) return;
            /*//点击用户名跳转
            holder.replyItem_userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PostActivity.this,userInfoActivity.class);
                    intent.putExtra("uid",reply.get(position).getUid());
                    startActivity(intent);
                }
            });
            //点击用户头像跳转
            holder.replyItem_user_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PostActivity.this,userInfoActivity.class);
                    intent.putExtra("uid",reply.get(position).getUid());
                    startActivity(intent);
                }
            });
            //点击查看更多回复
            holder.replyItem_viewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PostActivity.this,SubReplyActivity.class);
                    intent.putExtra("uid",reply.get(position).getId());
                    startActivity(intent);
                }
            });*/
            holder.updatePostReply(reply.get(position));
        }

        @Override
        public int getItemCount() {
            return reply.size();
        }

        public void setData(List<Reply> reply) {
            this.reply = reply;
            notifyDataSetChanged();
        }
    }

}
