# diamond-document-back-end

小学期敏捷开发项目。最终展现形式为PC的web网站。

具体流程部分不写在这里。这里具体写需求相关。

已明确的范围（无需实现的功能）：

**不需要在共享时实现类似腾讯文档的实时同步**，而只要求能够协作即可，即**不需要A改文档时B能够实时看到改变的内容**，而**只需要B看到文档在被修改**即可，这样以防止冲突。

**只要求实现doc文档这一种形式的共享，只是内容**，不需要存储实际的文件（？**图片应该需要存储**）。doc文档只是对内容进行一个大致的定义，即**文字+图片**的形式，而**不是特定为doc文件**，然后**在导入和下载上不做硬性要求**。

在内容编辑上推荐并且**仅允许大家使用富文本编辑器之类的模板**，以节省经费。要求内容上包括文字+图片，**对于格式没有硬性要求**，但是**需要有内容格式的调整**，甚至**使用md文法也可以**，只要**保证预览效果好**即可。有哪些格式由团队决定，需要从用户的角度出发。

/*消息**仅有这类系统消息，用户的私信功能不要求实现**。*/

在**不明确时去和甲方交流**，尽可能让项目满足甲方的设想。

本项目计划完成的功能有：

**基础功能**：注册、登录、个人信息的查看、完善与修改功能。

**个人工作台**：访问文档的起始点。最近浏览的文档、收藏的文档、自己创建的文档。

  **：权限**：个人工作台顾名思义，只是个人用的，私密的。访问他人主页时看到的是一个新的用户信息的页面，而不应该看到对方的工作台，因为我们“公开”这个概念，也只是在把文档分享出去之后才有的，如果不分享，所有的文档内容都是私密。（根据助教在论坛中的发言。）

  **：加入的团队**：访问加入的团队后，用户也应该能根据情况看到团队文档和信息等内容。

  **：回收站**：在一定情况下恢复删除的文档。

**文档**：创建、基于模板创建、修改、评论、分享等功能。

  **：创建修改**：文档应该最少有标题、内容、文档基础信息等内容。
  
  **：文档内容及语法**：要实现的是一种文字+图片的简单形式内容的文档，因此平台要有储存图片的功能。可以基于word的富文本编辑，也可以是markdown语法的编辑器，只要保证预览效果即可，预览时不能出现添加格式的符号。（根据助教在论坛中的发言。）
  
  **：协同效果**：A修改文档之后，B根据收到的分享内容，可以在刷新后看到内容的改变。因此也要有防止冲突的读锁和写锁，以及防止死锁。（根据助教在论坛中的发言。）

  **：权限**：文档应该有权限的设置和调整功能，不同的权限在查看、讨论、修改、分享等时候应该有区别，并且需要考虑到团队的存在。

  **：评论**：其他人或作者需要能够对特定文档增加评论，以作为建议或意见。类似于github的issue，可以和批注一样，评论时有具体对应的位置；也可以没有位置的对应，直接和文档放在一起展示即可，类似于帖子下面的一个个回复。（根据助教在论坛中的发言。）

  **：分享**：文档或者其他内容应该能以某种形式（例如链接、二维码、id信息），来分享给其他人，收到的人可以基于那种分享的内容去访问到对应的内容，同时访问时需要结合权限控制能力。
  
  **：版本控制**：创建时间、最后修改时间、修改次数。（根据助教在论坛中的发言。）

**团队**：类似于一个文件夹，这一“文件夹”至少要有创建者和成员两种身份，文件夹应该包括有团队文档和团队信息等内容。

  **：创建者**：应该有对团队进行调整的能力，例如管理成员、解散团队、设置文档权限。

  **：非创建者**：应该有对应的加入、退出团队等功能。

若还有时间，本项目可能完成的功能有：

**消息通知**：本系统中用户应该能接收消息或通知，它们范围没有具体定义，如下的内容均可作为消息来展示给用户（仅是示例）：团队邀请信息、加入退出团队的结果、踢出团队时的提醒、文档被评论的提醒等等。

以上。项目开始时间20.8.10，中期评比时间为20.8.14，项目完结时间20.8.20。
